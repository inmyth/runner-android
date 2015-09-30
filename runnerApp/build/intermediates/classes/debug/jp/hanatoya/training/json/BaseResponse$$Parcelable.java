
package jp.hanatoya.training.json;

import android.os.Parcelable;
import android.os.Parcelable.Creator;
import org.parceler.Generated;
import org.parceler.ParcelWrapper;

@Generated(value = "org.parceler.ParcelAnnotationProcessor", date = "2015-06-20T14:28+0900")
public class BaseResponse$$Parcelable
    implements Parcelable, ParcelWrapper<jp.hanatoya.training.json.BaseResponse>
{

    private jp.hanatoya.training.json.BaseResponse baseResponse$$0;
    @SuppressWarnings("UnusedDeclaration")
    public final static BaseResponse$$Parcelable.Creator$$0 CREATOR = new BaseResponse$$Parcelable.Creator$$0();

    public BaseResponse$$Parcelable(android.os.Parcel parcel$$0) {
        baseResponse$$0 = new jp.hanatoya.training.json.BaseResponse();
        baseResponse$$0 .error = parcel$$0 .readInt();
        baseResponse$$0 .message = parcel$$0 .readString();
    }

    public BaseResponse$$Parcelable(jp.hanatoya.training.json.BaseResponse baseResponse$$1) {
        baseResponse$$0 = baseResponse$$1;
    }

    @Override
    public void writeToParcel(android.os.Parcel parcel$$1, int flags) {
        parcel$$1 .writeInt(baseResponse$$0 .error);
        parcel$$1 .writeString(baseResponse$$0 .message);
    }

    @Override
    public int describeContents() {
        return  0;
    }

    @Override
    public jp.hanatoya.training.json.BaseResponse getParcel() {
        return baseResponse$$0;
    }

    private final static class Creator$$0
        implements Creator<BaseResponse$$Parcelable>
    {


        @Override
        public BaseResponse$$Parcelable createFromParcel(android.os.Parcel parcel$$2) {
            return new BaseResponse$$Parcelable(parcel$$2);
        }

        @Override
        public BaseResponse$$Parcelable[] newArray(int size) {
            return new BaseResponse$$Parcelable[size] ;
        }

    }

}
