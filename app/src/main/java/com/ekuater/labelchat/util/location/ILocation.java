
package com.ekuater.labelchat.util.location;

import com.ekuater.labelchat.datastruct.LocationInfo;

public interface ILocation {

    public LocationInfo getLocation();

    public void registerListener(final ILocationListener listener);

    public void unregisterListener();

    public void startLocation();

    public void stopLocation();
}
