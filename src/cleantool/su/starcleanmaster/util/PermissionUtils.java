package cleantool.su.starcleanmaster.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

public class PermissionUtils {

    public static boolean hasPermission(Context ctx, String permission) {
        return (ctx.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
    }

    public static void requestPermission(Activity ctx, String permission, int requestCode) {
        ctx.requestPermissions(new String[]{permission}, requestCode);
    }

    public static void requestPermissions(Activity ctx, String[] permissions, int requestCode) {
        ctx.requestPermissions(permissions, requestCode);
    }
}
