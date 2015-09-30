
package org.parceler;

import java.util.HashMap;
import java.util.Map;
import jp.hanatoya.training.json.BaseResponse;
import jp.hanatoya.training.json.BaseResponse$$Parcelable;
import jp.hanatoya.training.json.RunnerData;
import jp.hanatoya.training.json.RunnerData$$Parcelable;

@Generated(value = "org.parceler.ParcelAnnotationProcessor", date = "2015-06-20T14:28+0900")
@SuppressWarnings("unchecked")
public class Parceler$$Parcels
    implements Repository<org.parceler.Parcels.ParcelableFactory>
{

    private final Map<Class, org.parceler.Parcels.ParcelableFactory> map$$0 = new HashMap<Class, org.parceler.Parcels.ParcelableFactory>();

    public Parceler$$Parcels() {
        map$$0 .put(RunnerData.class, new Parceler$$Parcels.RunnerData$$Parcelable$$0());
        map$$0 .put(BaseResponse.class, new Parceler$$Parcels.BaseResponse$$Parcelable$$0());
    }

    public Map<Class, org.parceler.Parcels.ParcelableFactory> get() {
        return map$$0;
    }

    private final static class BaseResponse$$Parcelable$$0
        implements org.parceler.Parcels.ParcelableFactory<BaseResponse>
    {


        @Override
        public BaseResponse$$Parcelable buildParcelable(BaseResponse input) {
            return new BaseResponse$$Parcelable(input);
        }

    }

    private final static class RunnerData$$Parcelable$$0
        implements org.parceler.Parcels.ParcelableFactory<RunnerData>
    {


        @Override
        public RunnerData$$Parcelable buildParcelable(RunnerData input) {
            return new RunnerData$$Parcelable(input);
        }

    }

}
