package net.jpeelaer.hce.desfire;

import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jekkos on 8/24/15.
 */
public class SaveRestoreSessionTest extends AbstractAppletTest {

    @Test
    public void saveSession() throws IOException {
        createApplication(Util.TKTDES);

        Yaml yaml = new Yaml();
        String dump = yaml.dump(applet.getMasterFile());
        Object restoredDump = yaml.load(dump);

        Assert.assertTrue(restoredDump instanceof MasterFile);
        MasterFile masterFile = (MasterFile) restoredDump;
        Assert.assertTrue(masterFile.numberOfFiles() == 1);

    }

}
