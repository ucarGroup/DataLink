package com.ucar.datalink.domain.media.parameter.virtual;

import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;

        import java.util.ArrayList;
        import java.util.List;

public class VirtualMediaSrcParameter  extends MediaSrcParameter {

    private List<Long> realDbsId = new ArrayList<Long>();

    public List<Long> getRealDbsId() {
        return realDbsId;
    }

    public void setRealDbsId(List<Long> realDbsId) {
        this.realDbsId = realDbsId;
    }
}
