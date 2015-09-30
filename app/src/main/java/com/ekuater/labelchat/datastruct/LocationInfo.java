
package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Location info
 *
 * @author LinYong
 */
public class LocationInfo implements Parcelable {

    private double mLongitude;
    private double mLatitude;

    public LocationInfo() {
        mLongitude = 0.0D;
        mLatitude = 0.0D;
    }

    public LocationInfo(final LocationInfo location) {
        set(location);
    }

    public LocationInfo(double longitude, double latitude) {
        set(longitude, latitude);
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public void set(final LocationInfo location) {
        mLongitude = location.mLongitude;
        mLatitude = location.mLatitude;
    }

    public void set(double longitude, double latitude) {
        mLongitude = longitude;
        mLatitude = latitude;
    }

    @Override
    public String toString() {
        return String.valueOf(mLongitude) + "," + String.valueOf(mLatitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(mLongitude);
        dest.writeDouble(mLatitude);
    }

    public static final Parcelable.Creator<LocationInfo> CREATOR = new Parcelable.Creator<LocationInfo>() {

        @Override
        public LocationInfo createFromParcel(Parcel source) {
            LocationInfo location = new LocationInfo();

            location.mLongitude = source.readDouble();
            location.mLatitude = source.readDouble();

            return location;
        }

        @Override
        public LocationInfo[] newArray(int size) {
            return new LocationInfo[size];
        }
    };

    public double getDistance(LocationInfo other) {
        return distance(this, other);
    }

    public static LocationInfo build(String position) {
        if (TextUtils.isEmpty(position)) {
            return null;
        }

        String[] tmp = position.split(",");
        LocationInfo location = null;

        if (tmp.length >= 2) {
            try {
                double longitude = Double.valueOf(tmp[0]);
                double latitude = Double.valueOf(tmp[1]);
                location = new LocationInfo(longitude, latitude);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return location;
    }

    public static double distance(LocationInfo l1, LocationInfo l2) {
        return (l1 == l2) ? 0.0D
                : computeDistance(l1.mLatitude, l1.mLongitude, l2.mLatitude, l2.mLongitude);
    }

    private static double computeDistance(double lat1, double lon1,
                                          double lat2, double lon2) {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)

        int MAXITERS = 20;
        // Convert lat/long to radians
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;

        double a = 6378137.0; // WGS84 major axis
        double b = 6356752.3142; // WGS84 semi-major axis
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;

        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;

        double lambda = L; // initial guess
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
            sigma = Math.atan2(sinSigma, cosSigma); // (16)
            double sinAlpha = (sinSigma == 0) ? 0.0 :
                    cosU1cosU2 * sinLambda / sinSigma; // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 :
                    cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
            A = 1 + (uSquared / 16384.0) * // (3)
                    (4096.0 + uSquared *
                            (-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) * // (4)
                    (256.0 + uSquared *
                            (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) *
                    cosSqAlpha *
                    (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma * // (6)
                    (cos2SM + (B / 4.0) *
                            (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
                                    (B / 6.0) * cos2SM *
                                            (-3.0 + 4.0 * sinSigma * sinSigma) *
                                            (-3.0 + 4.0 * cos2SMSq)));

            lambda = L +
                    (1.0 - C) * f * sinAlpha *
                            (sigma + C * sinSigma *
                                    (cos2SM + C * cosSigma *
                                            (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }

        return (b * A * (sigma - deltaSigma));
    }
}
