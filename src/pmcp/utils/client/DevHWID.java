package pmcp.utils.client;

public class DevHWID {

    public static boolean isDev(String hwid) {
        return hwid.equals("89560e27b2e5922aaad4ba5e98e1f0dd2233ab569926ebd4924816b520dbc5bd")
                || hwid.equals("8d7dbcbcd81991c1ff4ec84f7fdc9839bdeac1a2ac0768a6bd590beee4bbf05a")
                || hwid.equals("51a5c41d3bbd492a6c4f14e6824ba655df0632320f3c95729c8400ec98d40d67")
                || hwid.equals("7f8bf160cea9eec623030c600ed718f8cbd8eefd97934a1d7a683ec2c5b5303a");
    }
}
