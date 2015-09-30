package com.ekuater.labelchat.ui.fragment.throwphoto;

import com.ekuater.labelchat.datastruct.LocationInfo;

/**
 * Created by Leo on 2015/1/10.
 *
 * @author LinYong
 */
public class LocUtil {

    private static final double PI = Math.PI;
    private static final double Rc = 6378137;  // 赤道半径
    private static final double Rj = 6356725;  // 极半径

    public static LocationInfo calcLocation(LocationInfo location, double distance,
                                            double angle) {
        JWD A = new JWD(location.getLongitude(), location.getLatitude());
        double dx = distance * sin(angle * PI / 180.);
        double dy = distance * cos(angle * PI / 180.);
        double BJD = (dx / A.Ed + A.m_RadLo) * 180. / PI;
        double BWD = (dy / A.Ec + A.m_RadLa) * 180. / PI;

        return new LocationInfo(BJD, BWD);
    }

    private static double sin(double d) {
        return Math.sin(d);
    }

    private static double cos(double d) {
        return Math.cos(d);
    }

    private static class JWD {

        double m_LoDeg, m_LoMin, m_LoSec;
        double m_LaDeg, m_LaMin, m_LaSec;
        double m_Longitude, m_Latitude;
        double m_RadLo, m_RadLa;
        double Ec;
        double Ed;

        public JWD(double longitude, double latitude) {
            m_LoDeg = (int) (longitude);
            m_LoMin = (int) ((longitude - m_LoDeg) * 60);
            m_LoSec = (longitude - m_LoDeg - m_LoMin / 60.) * 3600;

            m_LaDeg = (int) (latitude);
            m_LaMin = (int) ((latitude - m_LaDeg) * 60);
            m_LaSec = (latitude - m_LaDeg - m_LaMin / 60.) * 3600;

            m_Longitude = longitude;
            m_Latitude = latitude;
            m_RadLo = longitude * PI / 180.;
            m_RadLa = latitude * PI / 180.;
            Ec = Rj + (Rc - Rj) * (90. - m_Latitude) / 90.;
            Ed = Ec * cos(m_RadLa);
        }
    }
}
