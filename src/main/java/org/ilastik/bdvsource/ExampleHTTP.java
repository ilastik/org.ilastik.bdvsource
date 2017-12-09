package org.ilastik.bdvsource;

import static bdv.viewer.DisplayMode.SINGLE;
import static net.imglib2.cache.img.DiskCachedCellImgOptions.options;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import com.google.gson.Gson;
import net.imglib2.Interval;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.DiskCachedCellImgOptions.CacheType;
import net.imglib2.cache.util.IntervalKeyLoaderAsLongKeyLoader;
import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.volatiles.array.DirtyVolatileFloatArray;
import net.imglib2.img.basictypeaccess.volatiles.array.DirtyVolatileByteArray;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;

public class ExampleHTTP {

    public static void main(final String[] args) throws IOException {
        // load a specific dataset
        final String ilastikServerBaseUrl = "http://localhost:5000";
        // load a specific dataset
        HttpRequest loadProjectRequest = new HttpRequest(
                String.format("%s/api/project/load-project", ilastikServerBaseUrl));
        loadProjectRequest.post("{\"project_name\": \"drosophilaServerTest.ilp\"}");

        HttpRequest structuredInfoRequest = new HttpRequest(
                String.format("%s/api/workflow/get-structured-info", ilastikServerBaseUrl));
        String jsonString = structuredInfoRequest.get();
        Gson gson = new Gson();
        StructuredInfo loadedStructuredInfo;
        loadedStructuredInfo = gson.fromJson(jsonString, StructuredInfo.class);

        // for now, "mount" the last lane in the list:
        final String datasetName = loadedStructuredInfo.getDataSetName(loadedStructuredInfo.getNImageLanes() - 1);
        final String sourceNameRaw = "ImageGroup";
        StructuredInfo.ImageInfo dataSetInfoRaw = loadedStructuredInfo.getSourceInfo(datasetName, sourceNameRaw);

        final int[] cellDimensions = new int[]{64, 64, 64};
        // "axes": "tczyx", "shape": [1, 3, 200, 402, 202]
        final long[] dimensionsRaw = {
            dataSetInfoRaw.shape[4], // z
            dataSetInfoRaw.shape[3], // y
            dataSetInfoRaw.shape[2], // x
        };
        final String dataFormatRaw = "raw";
        final CellGrid gridRaw = new CellGrid(dimensionsRaw, cellDimensions);
        // --------------------------------------------------------------
        // Add raw data source
        final Bdv bdv;
        final SharedQueue queue = new SharedQueue(20);
        {
            // GET ilastik-server:4564/api/workflow/get-data/{dataset_name}/{source_name}/{format}/{tb_cb_zb_yb_xb}/{te_ce_ze_ye_xe}
            final String endPoint = "api/workflow/get-data";
            final String format = String.format("%s/%s/%s/%s/%s/%s/%s",
                    ilastikServerBaseUrl,
                    endPoint,
                    datasetName,
                    sourceNameRaw,
                    dataFormatRaw,
                    "0_0_%d_%d_%d",
                    "1_1_%d_%d_%d");
            System.out.println("format: " + format);

            final Function< Interval, String> addressComposer = interval -> {
                final String address = String.format(
                        format,
                        interval.min(2),
                        interval.min(1),
                        interval.min(0),
                        1 + interval.max(2),
                        1 + interval.max(1),
                        1 + interval.max(0));
                return address;
            };
            final BiConsumer< byte[], DirtyVolatileByteArray> copier = (bytes, access)
                    -> {
                System.arraycopy(bytes, 0, access.getCurrentStorageArray(), 0, bytes.length);
                access.setDirty();
            };
            final HTTPLoader< DirtyVolatileByteArray> functor = new HTTPLoader<>(addressComposer, (n) -> new DirtyVolatileByteArray((int) n, true), copier);
            final IntervalKeyLoaderAsLongKeyLoader< DirtyVolatileByteArray> loader = new IntervalKeyLoaderAsLongKeyLoader<>(gridRaw, functor);

            final DiskCachedCellImgOptions factoryOptions = options()
                    .cacheType(CacheType.BOUNDED)
                    .maxCacheSize(1000)
                    .cellDimensions(cellDimensions);

            final Img< UnsignedByteType> httpImg = new DiskCachedCellImgFactory< UnsignedByteType>(factoryOptions)
                    .createWithCacheLoader(dimensionsRaw, new UnsignedByteType(), loader);

            final BdvSource httpSource = BdvFunctions.show(
                    VolatileViews.wrapAsVolatile(httpImg, queue),
                    "ilastik");

            bdv = httpSource;
            bdv.getBdvHandle().getViewerPanel().setDisplayMode(SINGLE);
            httpSource.setDisplayRange(0.0, 255.0);
        }
        // --------------------------------------------------------------
        // Add prediction source
        // for now, "mount" the last lane in the list:
        final String sourceNamePred = "CachedPredictionProbabilities";
        StructuredInfo.ImageInfo dataSetInfoPred = loadedStructuredInfo.getSourceInfo(datasetName, sourceNamePred);

        final int[] cellDimensionsPred = new int[]{64, 64, 64};
        // "axes": "tczyx", "shape": [1, 3, 200, 402, 202]
        final long[] dimensionsPred = {
            dataSetInfoPred.shape[4], // z
            dataSetInfoPred.shape[3], // y
            dataSetInfoPred.shape[2], // x
        };
        final String dataFormatPred = "raw";
        final long nChannels = dataSetInfoPred.shape[1];
        final CellGrid gridPred = new CellGrid(dimensionsPred, cellDimensionsPred);
        for (long c = 0; c < nChannels; c++) {
            final String endPoint = "api/workflow/get-data";
            final String format = String.format("%s/%s/%s/%s/%s/%s/%s",
                    ilastikServerBaseUrl,
                    endPoint,
                    datasetName,
                    sourceNamePred,
                    dataFormatPred,
                    "0_%d_%d_%d_%d",
                    "1_%d_%d_%d_%d");
            System.out.println("format: " + format);

            System.out.println("format: " + format);
            final long tmp = c;
            final Function< Interval, String> addressComposer = interval -> {
                final String address = String.format(
                        format,
                        tmp,
                        interval.min(2),
                        interval.min(1),
                        interval.min(0),
                        1 + tmp,
                        1 + interval.max(2),
                        1 + interval.max(1),
                        1 + interval.max(0));
                return address;
            };
            final BiConsumer< byte[], DirtyVolatileFloatArray> copier = (bytes, access)
                    -> {
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                FloatBuffer fb = bb.asFloatBuffer();
                for (int idx = 0; idx < fb.capacity(); idx++) {
                    fb.put(idx, fb.get(idx));
                }
                fb.get(access.getCurrentStorageArray());
                access.setDirty();
            };
            final HTTPLoader< DirtyVolatileFloatArray> functor = new HTTPLoader<>(addressComposer, (n) -> new DirtyVolatileFloatArray((int) n, true), copier);
            final IntervalKeyLoaderAsLongKeyLoader< DirtyVolatileFloatArray> loader = new IntervalKeyLoaderAsLongKeyLoader<>(gridPred, functor);

            final DiskCachedCellImgOptions factoryOptions = options()
                    .cacheType(CacheType.BOUNDED)
                    .maxCacheSize(1000)
                    .cellDimensions(cellDimensions);

            final Img< FloatType> httpImg = new DiskCachedCellImgFactory< FloatType>(factoryOptions)
                    .createWithCacheLoader(dimensionsPred, new FloatType(), loader);

            final BdvSource httpSource = BdvFunctions.show(
                    VolatileViews.wrapAsVolatile(httpImg, queue),
                    "Prediction Channel " + c,
                    BdvOptions.options().addTo(bdv));

            final Bdv bdvHttpSource = httpSource;
            bdvHttpSource.getBdvHandle().getViewerPanel().setDisplayMode(SINGLE);
            httpSource.setDisplayRange(0.0, 1.0);
            httpSource.setDisplayRangeBounds(0.0, 1.0);
        }
    }
}
