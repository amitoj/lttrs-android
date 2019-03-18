package rs.ltt.android.ui.fragment;


public class MailboxQueryFragment extends AbstractMailboxQueryFragment {
    @Override
    protected String getMailboxId() {
        return MailboxQueryFragmentArgs.fromBundle(getArguments()).getMailbox();
    }
}
