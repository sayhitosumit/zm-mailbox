package com.zimbra.qa.unittest;

import java.io.IOException;

import org.dom4j.DocumentException;
import org.junit.After;
import org.junit.Before;

import com.zimbra.common.localconfig.ConfigException;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;

/**
 * Test the Embedded Remote IMAP server, doing the necessary configuration to make it work.
 *
 * Note: Currently bypasses Proxy, the tests connect directly to the embedded IMAP server's port
 *
 * The actual tests that are run are in {@link SharedImapTests}
 */
public class TestRemoteImapShared extends SharedImapTests {

    @Before
    public void setUp() throws ServiceException, IOException, DocumentException, ConfigException  {
        saveImapConfigSettings();
        TestUtil.setLCValue(LC.imap_always_use_remote_store, String.valueOf(true));
        imapServer.setReverseProxyUpstreamImapServers(new String[] {});
        super.sharedSetUp();
    }

    @After
    public void tearDown() throws ServiceException, DocumentException, ConfigException, IOException  {
        super.sharedTearDown();
        restoreImapConfigSettings();
    }

    @Override
    protected int getImapPort() {
        return imapServer.getImapBindPort();
    }
}