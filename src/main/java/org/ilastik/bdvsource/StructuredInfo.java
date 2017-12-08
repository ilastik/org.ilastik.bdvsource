package org.ilastik.bdvsource;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 *
 * @author k-dominik
 */
public class StructuredInfo {
    public static class ImageInfo {
        public String name;
        public String axes;
        public long[] shape;
        public int lane_number;
        public String dtype;
        public String dataset_name;
        public String source_name;
    }

    public ImageInfo[][] states;
    public String[] image_names;
    public String message;

    public int getNImageLanes() {
        return states.length;
    }

    public String getDataSetName(int laneIndex){
        return image_names[laneIndex];
    }

    public int getLaneIndexForDataSet(String datasetName){
        return Arrays.asList(image_names).indexOf(datasetName);
    }

    public ImageInfo getSourceInfo(String datasetName, String SourceName){
        System.out.println("SourceName: " + SourceName);
        int laneIndex = getLaneIndexForDataSet(datasetName);
        ImageInfo[] laneArray = states[laneIndex];
        OptionalInt foundIndex = IntStream.range(0, laneArray.length)
            .filter(infoIndex-> laneArray[infoIndex].source_name.equals(SourceName))
            .findFirst();
        int sourceIndex = -1;
        if (foundIndex.isPresent()) {
            sourceIndex = foundIndex.getAsInt();
        } else {
            throw new NoSuchElementException();
        }
        return states[laneIndex][sourceIndex];
    }
}
