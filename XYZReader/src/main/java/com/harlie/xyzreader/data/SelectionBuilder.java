/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications:
 * -Imported from AOSP frameworks/base/core/java/com/android/internal/content
 * -Changed package name
 */

package com.harlie.xyzreader.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Helper for building selection clauses for {@link SQLiteDatabase}. Each
 * appended clause is combined using {@code AND}. This class is <em>not</em>
 * thread safe.
 */
public class SelectionBuilder {
    private final static String TAG = "LEE: <" + SelectionBuilder.class.getSimpleName() + ">";

    private String mTable = null;
    private StringBuilder mSelection;
    private ArrayList<String> mSelectionArgs;

    /**
     * Append the given selection clause to the internal state. Each clause is
     * surrounded with parenthesis and combined using {@code AND}.
     */
    public SelectionBuilder where(String selection, String... selectionArgs) {
        Log.v(TAG, "where");
        if (TextUtils.isEmpty(selection)) {
            if (selectionArgs != null && selectionArgs.length > 0) {
                throw new IllegalArgumentException(
                        "Valid selection required when including arguments=");
            }

            // Shortcut when clause is empty
            return this;
        }

        ensureSelection(selection.length());
        if (mSelection.length() > 0) {
            mSelection.append(" AND ");
        }

        mSelection.append("(").append(selection).append(")");
        if (selectionArgs != null) {
            ensureSelectionArgs();
            Collections.addAll(mSelectionArgs, selectionArgs);
        }

        return this;
    }

    public SelectionBuilder table(@SuppressWarnings("SameParameterValue") String table) {
        Log.v(TAG, "table");
        mTable = table;
        return this;
    }

    private void assertTable() {
        Log.v(TAG, "assertTable");
        if (mTable == null) {
            throw new IllegalStateException("Table not specified");
        }
    }

    private void ensureSelection(int lengthHint) {
        Log.v(TAG, "ensureSelection");
        if (mSelection == null) {
            mSelection = new StringBuilder(lengthHint + 8);
        }
    }

    private void ensureSelectionArgs() {
        Log.v(TAG, "ensureSelectionArgs");
        if (mSelectionArgs == null) {
            mSelectionArgs = new ArrayList<>();
        }
    }

    /**
     * Return selection string for current internal state.
     *
     * @see #getSelectionArgs()
     */
    private String getSelection() {
        Log.v(TAG, "getSelection");
        if (mSelection != null) {
            return mSelection.toString();
        } else {
            return null;
        }
    }

    /**
     * Return selection arguments for current internal state.
     *
     * @see #getSelection()
     */
    private String[] getSelectionArgs() {
        Log.v(TAG, "getSelectionArgs");
        if (mSelectionArgs != null) {
            return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        Log.v(TAG, "toString");
        return "SelectionBuilder[table=" + mTable + ", selection=" + getSelection()
                + ", selectionArgs=" + Arrays.toString(getSelectionArgs()) + "]";
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(SQLiteDatabase db, String[] columns, String orderBy) {
        Log.v(TAG, "query");
        return query(db, columns, null, null, orderBy, null);
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    private Cursor query(SQLiteDatabase db,
                         String[] columns,
                         @SuppressWarnings("SameParameterValue") String groupBy,
                         @SuppressWarnings("SameParameterValue") String having,
                         String orderBy,
                         @SuppressWarnings("SameParameterValue") String limit) {
        Log.v(TAG, "query");
        assertTable();
        return db.query(mTable, columns, getSelection(), getSelectionArgs(), groupBy, having,
                orderBy, limit);
    }

    /**
     * Execute update using the current internal state as {@code WHERE} clause.
     */
    public int update(SQLiteDatabase db, ContentValues values) {
        Log.v(TAG, "update");
        assertTable();
        return db.update(mTable, values, getSelection(), getSelectionArgs());
    }

    /**
     * Execute delete using the current internal state as {@code WHERE} clause.
     */
    public int delete(SQLiteDatabase db) {
        Log.v(TAG, "delete");
        assertTable();
        return db.delete(mTable, getSelection(), getSelectionArgs());
    }
}
