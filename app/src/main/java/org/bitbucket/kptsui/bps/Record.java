package org.bitbucket.kptsui.bps;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Record {
    public final static int ONE_MINUTE = 60000;
    public final static int ONE_HOUR = 3600000;
    public final static int ONE_DAY = 86400000;
    public final static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yy");

    public String carLotId;

    public String payment;
    public int parkedTime;
    public String checkinTime;
    public String checkoutTime;

    public Record(String carLotId, String payment, Date checkinTime, Date checkoutTime){
        this.carLotId = carLotId;
        this.payment = payment == null ? "" : payment;
        this.checkinTime = checkinTime == null ? "---" : sdf.format(checkinTime);
        this.checkoutTime = checkoutTime == null ? "---" : sdf.format(checkoutTime);

        if(checkinTime != null && checkoutTime != null){
            long diff = checkoutTime.getTime() - checkinTime.getTime();
            this.parkedTime = (int) (diff / ONE_HOUR);
        }
        else if (checkoutTime == null && checkinTime != null){
            long diff = new Date().getTime() - checkinTime.getTime();
            this.parkedTime = (int) (diff / ONE_HOUR);
        }
    }
}
