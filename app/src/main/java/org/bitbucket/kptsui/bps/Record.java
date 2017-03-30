package org.bitbucket.kptsui.bps;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Record {
    public final static int ONE_MINUTE = 60000;
    public final static int ONE_HOUR = 3600000;
    public final static int ONE_DAY = 86400000;
    public final static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/YY");

    public String carLotId;

    public String payment;
    public int parkedTime;
    public String checkinTime;
    public String checkoutTime;

    public Record(String carLotId, String payment, Date checkinTime, Date checkoutTime){
        this.carLotId = carLotId;
        this.payment = payment == null ? "" : payment;
        this.checkinTime = sdf.format(checkinTime);
        this.checkoutTime = sdf.format(checkoutTime);

        long diff = checkoutTime.getTime() - checkinTime.getTime();
        this.parkedTime = (int) (diff / ONE_HOUR);
    }
}
