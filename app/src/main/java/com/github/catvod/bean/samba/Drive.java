package com.github.catvod.bean.samba;

import android.net.Uri;
import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class Drive {

    @SerializedName("name")
    private String name;
    @SerializedName("server")
    private String server;

    private Connection connection;
    private SMBClient smbClient;
    private DiskShare diskShare;
    private Session session;
    private String subPath;

    public static List<Drive> arrayFrom(String str) {
        Type listType = new TypeToken<List<Drive>>() {}.getType();
        return new Gson().fromJson(str, listType);
    }

    public Drive(String name) {
        this.name = name;
    }

    private String getName() {
        return TextUtils.isEmpty(name) ? "" : name;
    }

    public String getServer() {
        return TextUtils.isEmpty(server) ? "" : server;
    }

    public String getSubPath() {
        return TextUtils.isEmpty(subPath) ? "" : subPath;
    }

    public DiskShare getShare() {
        if (diskShare == null) init();
        return diskShare;
    }

    public Class toType() {
        return new Class(getName(), getName(), "1");
    }

    /**
     * 初始化 SMB 连接
     * <p>
     * 改进异常处理，使用 Logger 记录错误。
     * </p>
     */
    private void init() {
        try {
            smbClient = new SMBClient();
            Uri uri = Uri.parse(getServer());

            if (uri.getPath() == null || uri.getPath().length() <= 1) {
                Logger.w("Invalid SMB path: " + getServer());
                return;
            }

            String[] parts = uri.getPath().substring(1).split("/", 2);
            if (parts.length == 0 || parts[0].isEmpty()) {
                Logger.w("Invalid share name in SMB path: " + getServer());
                return;
            }

            int port = uri.getPort() != -1 ? uri.getPort() : SMBClient.DEFAULT_PORT;
            connection = smbClient.connect(uri.getHost(), port);
            session = connection.authenticate(getAuthentication(uri));
            diskShare = (DiskShare) session.connectShare(parts[0]);
            subPath = parts.length > 1 ? parts[1] : "";

            Logger.i("SMB connection initialized: " + getName());

        } catch (IOException e) {
            Logger.e("Network error while connecting to SMB: " + getServer(), e);
        } catch (IllegalArgumentException e) {
            Logger.e("Invalid SMB server URL: " + getServer(), e);
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.e("Invalid SMB path format: " + getServer(), e);
        }
    }

    /**
     * 获取 SMB 认证信息
     *
     * @param uri SMB URI
     * @return 认证上下文
     */
    private AuthenticationContext getAuthentication(Uri uri) {
        String userInfo = uri.getUserInfo();
        if (userInfo == null) {
            Logger.d("Using guest authentication for SMB");
            return AuthenticationContext.guest();
        }

        String[] parts = userInfo.split(":", 2);
        String username = parts[0];
        char[] password = parts.length > 1 ? parts[1].toCharArray() : new char[0];

        Logger.d("Using username/password authentication for SMB: " + username);
        return new AuthenticationContext(username, password, null);
    }

    /**
     * 释放 SMB 连接资源
     * <p>
     * 改进异常处理，确保所有资源都被释放。
     * </p>
     */
    public void release() {
        try {
            if (diskShare != null) {
                diskShare.close();
            }
        } catch (IOException e) {
            Logger.e("Failed to close disk share", e);
        }

        try {
            if (session != null) {
                session.close();
            }
        } catch (IOException e) {
            Logger.e("Failed to close session", e);
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (IOException e) {
            Logger.e("Failed to close connection", e);
        }

        try {
            if (smbClient != null) {
                smbClient.close();
            }
        } catch (IOException e) {
            Logger.e("Failed to close SMB client", e);
        } finally {
            connection = null;
            diskShare = null;
            smbClient = null;
            session = null;
            Logger.d("SMB resources released");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Drive)) return false;
        Drive it = (Drive) obj;
        return getName().equals(it.getName());
    }
}
