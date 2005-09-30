/*
 * Created on Sep 27, 2005
 */
package com.zimbra.cs.service.util;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.service.ServiceException;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraContext;

/**
 * @author dkarp
 */
public class ItemId {
    private static final char ACCOUNT_DELIMITER = ':';
    private static final char PART_DELIMITER    = '-';

    private String mAccountId;
    private int    mId;
    private int    mSubpartId = -1;

    public ItemId(MailItem item) {
        this(item.getMailbox(), item.getId());
    }
    public ItemId(Mailbox mbox, int id) {
        this(mbox.getAccountId(), id);
    }
    public ItemId(String acctId, int id) {
        mAccountId = acctId;  mId = id;
    }
    public ItemId(MailItem item, int subId) {
        this(item.getMailbox().getAccountId(), item.getId(), subId);
    }
    public ItemId(String acctId, int id, int subId) {
        mAccountId = acctId;  mId = id;  mSubpartId = subId;
    }

    public ItemId(String encoded) throws ServiceException {
        if (encoded == null || encoded.equals(""))
            throw ServiceException.INVALID_REQUEST("empty/missing item ID", null);

        // strip off the account id, if present
        int delimiter = encoded.indexOf(ACCOUNT_DELIMITER);
        if (delimiter == 0 || delimiter == encoded.length() - 1)
            throw ServiceException.INVALID_REQUEST("malformed item ID: " + encoded, null);
        if (delimiter != -1)
            mAccountId = encoded.substring(0, delimiter);
        encoded = encoded.substring(delimiter + 1);

        // break out the appointment sub-id, if present
        delimiter = encoded.indexOf(PART_DELIMITER);
        if (delimiter == encoded.length() - 1)
            throw ServiceException.INVALID_REQUEST("malformed item ID: " + encoded, null);
        try {
            if (delimiter > 0) {
                mSubpartId = Integer.parseInt(encoded.substring(delimiter + 1));
                if (mSubpartId < 0)
                    throw ServiceException.INVALID_REQUEST("malformed item ID: " + encoded, null);
                encoded = encoded.substring(0, delimiter);
            }
            mId = Integer.parseInt(encoded);
        } catch (NumberFormatException nfe) {
            throw ServiceException.INVALID_REQUEST("malformed item ID: " + encoded, nfe);
        }
    }

    public String getAccountId()  { return mAccountId; }
    public int getId()            { return mId; }
    public int getSubpartId()     { return mSubpartId; }

    public boolean hasSubpart()   { return mSubpartId >= 0; }

    public boolean isLocal() throws ServiceException {
        if (mAccountId == null)
            return true;
        Account acctTarget = Provisioning.getInstance().getAccountById(mAccountId);
        if (acctTarget == null)
            throw AccountServiceException.NO_SUCH_ACCOUNT(mAccountId);
        return DocumentHandler.LOCAL_HOST.equalsIgnoreCase(acctTarget.getAttr(Provisioning.A_zimbraMailHost));
    }

    public boolean belongsTo(Account acct) {
        return acct == null || mAccountId == null || mAccountId.equals(acct.getId());
    }
    public boolean belongsTo(String acctId) {
        return acctId == null || mAccountId == null || mAccountId.equals(acctId);
    }
    public boolean belongsTo(Mailbox mbox) {
        return mbox == null || mAccountId == null || mAccountId.equals(mbox.getAccountId());
    }

    public String toString()  { return toString((String) null); }
    public String toString(Account authAccount) {
        return toString(authAccount == null ? null : authAccount.getId());
    }
    public String toString(ZimbraContext lc) {
        return toString(lc == null ? null : lc.getAuthtokenAccountId());
    }
    public String toString(String authAccountId) {
        StringBuffer sb = new StringBuffer();
        if (mAccountId != null && mAccountId.length() > 0 && !mAccountId.equals(authAccountId))
            sb.append(mAccountId).append(ACCOUNT_DELIMITER);
        sb.append(mId);
        if (hasSubpart())
            sb.append(PART_DELIMITER).append(mSubpartId);
        return sb.toString();
    }
}
