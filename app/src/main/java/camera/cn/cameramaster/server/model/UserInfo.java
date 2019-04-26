package camera.cn.cameramaster.server.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 用户
 *
 * @packageName: ymc.cn.servertest.model
 * @fileName: UserInfo
 * @date: 2019/4/23  17:31
 * @author: ymc
 * @QQ:745612618
 */

public class UserInfo implements Parcelable {

    @JSONField(name = "userId")
    private String mUserId;
    @JSONField(name = "userName")
    private String mUserName;

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserId);
        dest.writeString(mUserName);
    }

    public UserInfo() {
    }

    protected UserInfo(Parcel in) {
        mUserId = in.readString();
        mUserName = in.readString();
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getmUserName() {
        return mUserName;
    }

    public void setmUserName(String mUserName) {
        this.mUserName = mUserName;
    }
}
