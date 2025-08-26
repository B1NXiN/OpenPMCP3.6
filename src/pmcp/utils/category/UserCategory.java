package pmcp.utils.category;

public enum UserCategory {

    FEE("正版用户", "Official"), FREE("免费用户", "Free"),DEV("开发人员", "Dev");

    public final String c;
    public final String e;

    UserCategory(String c, String e) {
        this.c = c;
        this.e = e;
    }
}
