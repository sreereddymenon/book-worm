package com.ronakmanglani.booknerd.ui.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ronakmanglani.booknerd.BookNerdApp;
import com.ronakmanglani.booknerd.R;
import com.ronakmanglani.booknerd.data.BookColumns;
import com.ronakmanglani.booknerd.data.BookProvider;
import com.ronakmanglani.booknerd.model.Book;
import com.ronakmanglani.booknerd.ui.activity.BookActivity;
import com.ronakmanglani.booknerd.ui.activity.MainActivity;
import com.ronakmanglani.booknerd.ui.adapter.BookCursorAdapter;
import com.ronakmanglani.booknerd.ui.adapter.BookCursorAdapter.OnBookClickListener;
import com.ronakmanglani.booknerd.ui.view.PaddingDecorationView;
import com.ronakmanglani.booknerd.util.DimenUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ListFragment extends Fragment implements OnBookClickListener, LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 42;

    private View rootView;
    private int currentShelf;
    private Unbinder unbinder;
    private BookCursorAdapter adapter;

    @BindView(R.id.book_list)           RecyclerView bookList;
    @BindView(R.id.progress_circle)     View progressCircle;
    @BindView(R.id.list_placeholder)    View listPlaceholder;

    // Fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list, container, false);

        // Initialization
        setRetainInstance(true);
        unbinder = ButterKnife.bind(this, rootView);
        currentShelf = getArguments().getInt(BookNerdApp.KEY_SHELF);

        // Setup RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), DimenUtil.getNumberOfColumns(R.dimen.book_card_width, 1));
        adapter = new BookCursorAdapter(this);
        bookList.setHasFixedSize(true);
        bookList.setLayoutManager(layoutManager);
        bookList.addItemDecoration(new PaddingDecorationView(getContext(), R.dimen.recycler_item_padding));
        bookList.setAdapter(adapter);

        // Load books from database
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        return rootView;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    // Load books from database
    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {
        switch (loaderID) {
            case CURSOR_LOADER_ID:
                // Returns a new CursorLoaders
                return new CursorLoader(getContext(),
                        BookProvider.Books.CONTENT_URI, new String[]{ },
                        BookColumns.SHELF + " = '" + currentShelf + "'",
                        null, null);
            default:
                // An invalid id was passed in
                return null;
        }
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            listPlaceholder.setVisibility(View.GONE);
            progressCircle.setVisibility(View.GONE);
            bookList.setVisibility(View.VISIBLE);
            adapter.swapCursor(data);
        } else {
            bookList.setVisibility(View.GONE);
            progressCircle.setVisibility(View.GONE);
            listPlaceholder.setVisibility(View.VISIBLE);
        }
        // If tablet: Load book fragment
        if (DimenUtil.isTablet()) {
            rootView.post(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity) getActivity()).loadDetailFragmentWith(adapter.getItemAt(0), false);
                }
            });
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    // Click events
    @Override
    public void onBookClicked(int position) {
        Book book = adapter.getItemAt(position);
        if (DimenUtil.isTablet()) {
            ((MainActivity) getActivity()).loadDetailFragmentWith(book, false);
        } else {
            Intent intent = new Intent(getContext(), BookActivity.class);
            intent.putExtra(BookNerdApp.KEY_BOOK, book);
            startActivity(intent);
        }
    }
}