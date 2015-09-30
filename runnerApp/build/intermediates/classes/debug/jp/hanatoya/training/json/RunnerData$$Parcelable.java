
package jp.hanatoya.training.json;

import android.os.Parcelable;
import android.os.Parcelable.Creator;
import org.parceler.Generated;
import org.parceler.ParcelWrapper;

@Generated(value = "org.parceler.ParcelAnnotationProcessor", date = "2015-06-20T14:28+0900")
public class RunnerData$$Parcelable
    implements Parcelable, ParcelWrapper<jp.hanatoya.training.json.RunnerData>
{

    private jp.hanatoya.training.json.RunnerData runnerData$$0;
    @SuppressWarnings("UnusedDeclaration")
    public final static RunnerData$$Parcelable.Creator$$1 CREATOR = new RunnerData$$Parcelable.Creator$$1();

    public RunnerData$$Parcelable(android.os.Parcel parcel$$3) {
        runnerData$$0 = new jp.hanatoya.training.json.RunnerData();
        runnerData$$0 .ps = parcel$$3 .readFloat();
        runnerData$$0 .distance = parcel$$3 .readInt();
        runnerData$$0 .runnerName = parcel$$3 .readString();
        runnerData$$0 .stopMsCoach = parcel$$3 .readLong();
        runnerData$$0 .step = parcel$$3 .readInt();
        runnerData$$0 .pitch = parcel$$3 .readFloat();
        runnerData$$0 .stride = parcel$$3 .readFloat();
        runnerData$$0 .ip3 = parcel$$3 .readString();
        runnerData$$0 .speed = parcel$$3 .readFloat();
        runnerData$$0 .error = parcel$$3 .readInt();
        runnerData$$0 .message = parcel$$3 .readString();
    }

    public RunnerData$$Parcelable(jp.hanatoya.training.json.RunnerData runnerData$$1) {
        runnerData$$0 = runnerData$$1;
    }

    @Override
    public void writeToParcel(android.os.Parcel parcel$$4, int flags) {
        parcel$$4 .writeFloat(runnerData$$0 .ps);
        parcel$$4 .writeInt(runnerData$$0 .distance);
        parcel$$4 .writeString(runnerData$$0 .runnerName);
        parcel$$4 .writeLong(runnerData$$0 .stopMsCoach);
        parcel$$4 .writeInt(runnerData$$0 .step);
        parcel$$4 .writeFloat(runnerData$$0 .pitch);
        parcel$$4 .writeFloat(runnerData$$0 .stride);
        parcel$$4 .writeString(runnerData$$0 .ip3);
        parcel$$4 .writeFloat(runnerData$$0 .speed);
        parcel$$4 .writeInt(runnerData$$0 .error);
        parcel$$4 .writeString(runnerData$$0 .message);
    }

    @Override
    public int describeContents() {
        return  0;
    }

    @Override
    public jp.hanatoya.training.json.RunnerData getParcel() {
        return runnerData$$0;
    }

    private final static class Creator$$1
        implements Creator<RunnerData$$Parcelable>
    {


        @Override
        public RunnerData$$Parcelable createFromParcel(android.os.Parcel parcel$$5) {
            return new RunnerData$$Parcelable(parcel$$5);
        }

        @Override
        public RunnerData$$Parcelable[] newArray(int size) {
            return new RunnerData$$Parcelable[size] ;
        }

    }

}
