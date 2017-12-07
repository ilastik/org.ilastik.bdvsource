package org.ilastik.bdvsource;

/**
 *
 * @author k-dominik
 */
public class StructuredInfo {
    public static class ImageInfo {
        public String name;
        public String axes;
        public int[] shape;
        public int lane_number;
        public String dtype;
        public String dataset_name;
        public String source_name;
    }

    public ImageInfo[][] states;
    public String[] image_names;
    public String message;
}
