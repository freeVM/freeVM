/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/** 
 * @author Elena V. Sayapina 
 * @version $Revision: 1.4 $ 
 */ 

package javax.print.attribute;

import java.io.Serializable;


public abstract class ResolutionSyntax implements Cloneable, Serializable {


    public static final int DPI = 100;

    public static final int DPCM = 254;


    private int crossFeedRes;

    private int feedRes;

    
    public ResolutionSyntax(int crossFeedResolution,
                                        int feedResolution, int units) {

        if (crossFeedResolution < 1) {
            throw new IllegalArgumentException("CrossFeedResolution " +
                                                    "is less than 1");
        }
        if (feedResolution < 1) {
            throw new IllegalArgumentException("FeedResolution is less than 1");
        }
        if (units < 1) {
            throw new IllegalArgumentException("Units is less than 1");
        }
        crossFeedRes = crossFeedResolution * units;
        feedRes = feedResolution * units;
    }


    public boolean equals(Object object) {

        if ((object instanceof ResolutionSyntax) &&
                crossFeedRes == ((ResolutionSyntax) object).crossFeedRes &&
                feedRes == ((ResolutionSyntax) object).feedRes) {
                            return true;
        } else {
            return false;
        }

    }

    public int getCrossFeedResolution(int units) {

        if (units < 1) {
            throw new IllegalArgumentException("units is less than 1");
        }
        return Math.round( ((float) crossFeedRes)/units);

    }

    protected int getCrossFeedResolutionDphi() {
        return crossFeedRes;
    }

    public int getFeedResolution (int units) {

        if (units < 1) {
            throw new IllegalArgumentException("units is less than 1");
        }
        return Math.round( ((float) feedRes)/units);
    }

    protected int getFeedResolutionDphi() {
        return feedRes;
    }

    public int[] getResolution(int units) {
        return new int[] { getCrossFeedResolution(units),
                           getFeedResolution(units) };
    }

    public int hashCode() {
        return ( crossFeedRes | (feedRes << 16) );
    }

    public boolean lessThanOrEquals (ResolutionSyntax resolutionSyntax) {

        if ( crossFeedRes <= resolutionSyntax.crossFeedRes &&
                feedRes <= resolutionSyntax.feedRes ) {
                    return true;
        } else {
            return false;
        }

    }

    public String toString() {
        return (crossFeedRes + "x" + feedRes + " dphi");
    }

    public String toString (int units, String unitsName) {
        if (unitsName == null) {
            unitsName = "";
        }
        return (getCrossFeedResolution(units) + "x" + getFeedResolution(units) +
                    " " + unitsName);
    }


}
