package at.ac.tuwien.detlef.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * A DialogFragment for a progress dialog.
 */
public class ProgressDialogFragment extends DialogFragment {
    private static final String KEY_TITLE = "KEY_TITLE";
    private static final String KEY_MESSAGE = "KEY_MESSAGE";

    /**
     * Create a new instance of ProgressDialogFragment with the given title and message.
     * @param title The title for the dialog.
     * @param message The message for the dialog.
     * @return The newly constructed dialog.
     */
    public static ProgressDialogFragment newInstance(String title, String message) {
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_MESSAGE, message);

        ProgressDialogFragment ret = new ProgressDialogFragment();

        ret.setArguments(args);

        return ret;
    }

    /**
     * Dismiss the Fragment with the given tag from the given FragmentManager.
     * @param mgr The FragmentManager.
     * @param tag The tag of the Fragment we want to remove.
     */
    public static void dismiss(FragmentManager mgr, String tag) {
        FragmentTransaction ft = mgr.beginTransaction();

        Fragment frag = mgr.findFragmentByTag(tag);
        if (frag != null) {
            mgr.popBackStack(frag.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ft.remove(frag);
        }

        ft.commit();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = this.getArguments();
        final String title = args.getString(KEY_TITLE);
        final String message = args.getString(KEY_MESSAGE);

        ProgressDialog ret = new ProgressDialog(getActivity());

        ret.setTitle(title);
        ret.setMessage(message);

        return ret;
    }
}
