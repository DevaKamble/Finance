/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.money.manager.ex.investment;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.Constants;
import com.money.manager.ex.account.AccountEditActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.money.manager.ex.investment.events.PriceUpdateRequestEvent;
import com.money.manager.ex.servicelayer.AccountService;
import com.shamanland.fonticon.FontIconDrawable;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import info.javaperformance.money.Money;

/**
 * The main fragment for the watchlist. Contains the list and everything else.
 * Not sure why it was done in two fragments. Probably because the list can not have additional items?
 */
public class WatchlistFragment
    extends Fragment {

    private static final String KEY_ACCOUNT_ID = "WatchlistFragment:AccountId";
    private static final String KEY_ACCOUNT = "WatchlistFragment:Account";

    /**
     * @param accountId ID Account to be display
     * @return instance of Watchlist fragment with transactions for the given account.
     */
    public static WatchlistFragment newInstance(int accountId) {
        WatchlistFragment fragment = new WatchlistFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_ACCOUNT_ID, accountId);
        fragment.setArguments(args);

        fragment.setNameFragment(WatchlistFragment.class.getSimpleName() + "_" + Integer.toString(accountId));

        return fragment;
    }

    private WatchlistItemsFragment mDataFragment;
    private String mNameFragment;

    private Account mAccount;

    private ImageView imgAccountFav, imgGotoAccount;
    ViewGroup mListHeader;

    // price update counter. Used to know when all the prices are done downloading.
    private int mUpdateCounter;
    private int mToUpdateTotal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadAccount();

        if ((savedInstanceState != null)) {
            mAccount = savedInstanceState.getParcelable(KEY_ACCOUNT);
        }

        mUpdateCounter = 0;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null)) {
            mAccount = savedInstanceState.getParcelable(KEY_ACCOUNT);
        }

        if (container == null) return null;
        View view = inflater.inflate(R.layout.fragment_account_transactions, container, false);

        if (mAccount == null) {
            loadAccount();
        }

        initializeListHeader(inflater);

        // manage fragment
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        mDataFragment = WatchlistItemsFragment.newInstance();
        // set arguments and settings of fragment
        Bundle arguments = new Bundle();
        arguments.putInt(WatchlistItemsFragment.KEY_ACCOUNT_ID, getAccountId());
        mDataFragment.setArguments(arguments);

        mDataFragment.setListHeader(mListHeader);
        mDataFragment.setAutoStarLoader(false);

        // add fragment
        transaction.replace(R.id.fragmentContent, mDataFragment, getNameFragment());
        transaction.commit();

        // refresh user interface
        if (mAccount != null) {
            setImageViewFavorite();
        }
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // hide the title
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        initializeAccountsSelector();
        selectCurrentAccount();

        // restart loader
        startLoaderData();
    }

    // Menu

    /**
     * Called once when the menu is being created.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // add options menu for watchlist
        inflater.inflate(R.menu.menu_watchlist, menu);

        // call create option menu of fragment
        mDataFragment.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Called every time the menu is displayed.
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

    }

    /**
     * Handle menu item click.
     * Update prices.
     * @param item Menu item selected
     * @return indicator whether the selection was handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_update_prices:
                confirmPriceUpdate();
                break;
            case R.id.menu_export_prices:
                exportPrices();
                break;
            case R.id.menu_purge_history:
                purgePriceHistory();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_ACCOUNT, mAccount);
    }

    // Events

    public void onEvent(PriceDownloadedEvent event) {
        onPriceDownloaded(event.symbol, event.price, event.date);
    }

    public void onEvent(PriceUpdateRequestEvent event) {
        onPriceUpdateRequested(event.symbol);
    }

    /**
     * Called from asynchronous task when a first price is downloaded.
     * @param symbol Stock symbol
     * @param price Stock price
     * @param date Date of the price
     */
    private void onPriceDownloaded(String symbol, Money price, Date date) {
        // prices updated.

        if (StringUtils.isEmpty(symbol)) return;

        // update the current price of the stock.
        StockRepository repo = getStockRepository();
        repo.updateCurrentPrice(symbol, price);

        // save price history record.
        StockHistoryRepository historyRepo = mDataFragment.getStockHistoryRepository();
        historyRepo.addStockHistoryRecord(symbol, price, date);

        mUpdateCounter += 1;
        if (mUpdateCounter == mToUpdateTotal) {
            completePriceUpdate();
        }
    }

    /**
     * Price update requested from the securities list context menu.
     * @param symbol Stock symbol for which to fetch the price.
     */
    private void onPriceUpdateRequested(String symbol) {
        // reset counter & max.
        mToUpdateTotal = 1;
        mUpdateCounter = 0;

        // http://stackoverflow.com/questions/1005073/initialization-of-an-arraylist-in-one-line
        List<String> symbols = new ArrayList<>();
        symbols.add(symbol);

        ISecurityPriceUpdater updater = SecurityPriceUpdaterFactory.getUpdaterInstance(getActivity());
        updater.downloadPrices(symbols);
        // result received via onEvent.
    }

    /**
     * refresh UI, show favorite icon
     */
    private void setImageViewFavorite() {
        if (mAccount.getFavorite()) {
            imgAccountFav.setBackgroundResource(R.drawable.ic_star);
        } else {
            imgAccountFav.setBackgroundResource(R.drawable.ic_star_outline);
        }
    }

    /**
     * Start Loader to retrieve data
     */
    public void startLoaderData() {
        if (mDataFragment != null) {
            mDataFragment.reloadData();
        }
    }

    public String getNameFragment() {
        return mNameFragment;
    }

    public void setNameFragment(String fragmentName) {
        this.mNameFragment = fragmentName;
    }

    // Private

    private void completePriceUpdate() {
        // this call is made from async task so have to get back to the main thread.
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // refresh the data.
                mDataFragment.reloadData();

                // notify about db file change.
                DropboxHelper.notifyDataChanged();
            }
        });
    }

    private StockRepository mStockRepository;

    private StockRepository getStockRepository() {
        if (mStockRepository == null) {
            mStockRepository = new StockRepository(getActivity());
        }
        return mStockRepository;
    }

    private String[] getAllShownSymbols() {
        int itemCount = mDataFragment.getListAdapter().getCount();
        String[] result = new String[itemCount];

        for(int i = 0; i < itemCount; i++) {
            Cursor cursor = (Cursor) mDataFragment.getListAdapter().getItem(i);
            String symbol = cursor.getString(cursor.getColumnIndex(Stock.SYMBOL));

            result[i] = symbol;
        }

        return result;
    }

    private void exportPrices() {
        PriceCsvExport export = new PriceCsvExport(getActivity());
        boolean result = false;

        try {
            String prefix;
            if (mAccount != null) {
                prefix = mAccount.getName();
            } else {
                prefix = getActivity().getString(R.string.all_accounts);
            }
            result = export.exportPrices(mDataFragment.getListAdapter(), prefix);
        } catch (IOException ex) {
            ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
            handler.handle(ex, "exporting stock prices");
        }

        // todo: handle result. (?)
    }

    private void confirmPriceUpdate() {
        new AlertDialogWrapper.Builder(getContext())
                .setTitle(R.string.download)
                .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
                .setMessage(R.string.confirm_price_download)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // get the list of symbols
                        String[] symbols = getAllShownSymbols();
                        mToUpdateTotal = symbols.length;
                        mUpdateCounter = 0;

                        // update security prices
                        ISecurityPriceUpdater updater = SecurityPriceUpdaterFactory
                                .getUpdaterInstance(getContext());
                        updater.downloadPrices(Arrays.asList(symbols));
                        // results received via event

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();
    }

    private int getAccountId() {
        if (mAccount == null) {
            return Constants.NOT_SET;
        }

        return mAccount.getId();
    }

    private ActionBar getActionBar() {
        if (!(getActivity() instanceof AppCompatActivity)) return null;

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        return actionBar;
    }

    private void initializeAccountsSelector() {
        ActionBar actionBar = getActionBar();
        if (actionBar == null) return;

        actionBar.setDisplayShowTitleEnabled(false);

        actionBar.setCustomView(R.layout.spinner);
        actionBar.setDisplayShowCustomEnabled(true);

        Spinner spinner = getAccountsSpinner();
        if (spinner == null) return;

        // Load accounts into the spinner.
        AccountService accountService = new AccountService(getActivity());
        accountService.loadInvestmentAccountsToSpinner(spinner);

        // handle account switching.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // switch account.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                Account account = Account.from(cursor);

                int accountId = account.getId();
                switchAccount(accountId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initializeListHeader(LayoutInflater inflater) {
        mListHeader = (ViewGroup) inflater.inflate(R.layout.fragment_watchlist_header, null, false);

        // favorite icon
        imgAccountFav = (ImageView) mListHeader.findViewById(R.id.imageViewAccountFav);
        // set listener click on favorite icon for change image
        imgAccountFav.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mAccount.setFavorite(!mAccount.getFavorite());

                AccountRepository repo = new AccountRepository(getActivity());
                boolean saved = repo.update(mAccount);

                if (!saved) {
                    Toast.makeText(getActivity(),
                        getActivity().getResources().getString(R.string.db_update_failed),
                        Toast.LENGTH_LONG).show();
                } else {
                    setImageViewFavorite();
                }
            }
        });

        // Edit account
        imgGotoAccount = (ImageView) mListHeader.findViewById(R.id.imageViewGotoAccount);
        imgGotoAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountEditActivity.class);
                intent.putExtra(AccountEditActivity.KEY_ACCOUNT_ID, getAccountId());
                intent.setAction(Intent.ACTION_EDIT);
                startActivity(intent);
            }
        });
    }

    private Spinner getAccountsSpinner() {
        // get from custom view, not the menu.

        ActionBar actionBar = getActionBar();
        if (actionBar == null) return null;

        Spinner spinner = (Spinner) actionBar.getCustomView().findViewById(R.id.spinner);
        return spinner;
    }

    private void loadAccount() {
        int accountId = getArguments().getInt(KEY_ACCOUNT_ID);
        this.mAccount = new AccountRepository(getActivity()).load(accountId);
    }

    private void purgePriceHistory() {
        new AlertDialogWrapper.Builder(getContext())
            .setTitle(R.string.purge_history)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
            .setMessage(R.string.purge_history_confirmation)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    StockHistoryRepository history = new StockHistoryRepository(getActivity());
                    int deleted = history.deleteAllPriceHistory();

                    if (deleted > 0) {
                        DropboxHelper.notifyDataChanged();
                        Toast.makeText(getActivity(),
                            getActivity().getString(R.string.purge_history_complete), Toast.LENGTH_SHORT)
                            .show();
                    } else {
                        Toast.makeText(getActivity(),
                            getActivity().getString(R.string.purge_history_failed), Toast.LENGTH_SHORT)
                            .show();
                    }

                    dialog.dismiss();
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            })
            .create()
            .show();
    }

    /**
     * Select the current account in the accounts dropdown.
     */
    private void selectCurrentAccount() {
        Spinner spinner = getAccountsSpinner();
        if (spinner == null) return;

        // find account
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) spinner.getAdapter();
        if (adapter == null) return;

        Cursor cursor = adapter.getCursor();
        int position = Constants.NOT_SET;

        for (int i = 0; i < adapter.getCount(); i++) {
            cursor.moveToPosition(i);
            String accountIdString = cursor.getString(cursor.getColumnIndex(Account.ACCOUNTID));
            int accountId = Integer.parseInt(accountIdString);
            if (accountId == getAccountId()) {
                position = i;
                break;
            }
        }

        spinner.setSelection(position);
    }

    private void switchAccount(int accountId) {
        if (accountId == getAccountId()) return;

        // switch account. Reload transactions.
        mAccount = new AccountRepository(getActivity()).load(accountId);

        mDataFragment.accountId = accountId;
        mDataFragment.reloadData();

        // hide account details bar if all accounts are selected
        if (accountId == Constants.NOT_SET) {
            mDataFragment.getListView().removeHeaderView(mListHeader);
            mListHeader.setVisibility(View.GONE);
        } else {
            if (mDataFragment.getListView().getHeaderViewsCount() == 0) {
                mDataFragment.getListView().addHeaderView(mListHeader);
            }
            mListHeader.setVisibility(View.VISIBLE);
        }
    }
}
