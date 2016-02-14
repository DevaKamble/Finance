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
package com.money.manager.ex.datalayer;

import android.content.ContentValues;
import android.content.Context;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.utils.MyDatabaseUtils;

/**
 * A repository for Categories.
 */
public class CategoryRepository
    extends RepositoryBase{

    public CategoryRepository(Context context) {
        super(context, "category_v1", DatasetType.TABLE, "category");
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {"CATEGID AS _id",
            Category.CATEGID,
            Category.CATEGNAME};
    }

    public Category load(int id) {
        if (id == Constants.NOT_SET) return null;

        ContentValues cv = first(Category.CATEGID + "=?", MyDatabaseUtils.getArgsForId(id));
        Category category = new Category(cv);
        return category;
    }
}
