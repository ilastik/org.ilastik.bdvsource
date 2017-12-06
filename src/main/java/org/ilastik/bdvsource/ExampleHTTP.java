package org.ilastik.bdvsource;

import static bdv.viewer.DisplayMode.SINGLE;
import static net.imglib2.cache.img.DiskCachedCellImgOptions.options;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.Interval;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.DiskCachedCellImgOptions.CacheType;
import net.imglib2.cache.util.IntervalKeyLoaderAsLongKeyLoader;
import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.volatiles.array.DirtyVolatileByteArray;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class ExampleHTTP
{
	public static void main( final String[] args ) throws IOException
	{
		final int[] cellDimensions = new int[] { 64, 64, 64 };
        // "axes": "tczyx", "shape": [1, 3, 200, 402, 202]
		final long[] dimensions = new long[] { 202, 402, 200 };
		final CellGrid grid = new CellGrid( dimensions, cellDimensions );

        
        // load a specific dataset
        HttpRequest loadProjectRequest = new HttpRequest("http://localhost:5000/api/project/load-project");
        loadProjectRequest.post("{\"project_name\": \"bubbedibuu.ilp\"}");
        
        HttpRequest structuredInfoRequest = new HttpRequest("http://localhost:5000/api/workflow/get-structured-info");
        structuredInfoRequest.get();

		// GET ilastik-server:4564/api/workflow/get-data/{dataset_name}/{source_name}/{format}/{tb_cb_zb_yb_xb}/{te_ce_ze_ye_xe}
		final String format = String.format( "%s/%s/%s",
				"http://localhost:5000/api/workflow/get-data/denk_raw/ImageGroup/raw",
				"0_0_%d_%d_%d",
				"1_1_%d_%d_%d" );
		System.out.println( "format: " + format );
		final Function< Interval, String > addressComposer = interval -> {
			final String address = String.format(
					format,
                    interval.min( 2 ),
                    interval.min( 1 ),
                    interval.min( 0 ),
					1 + interval.max( 2 ),
					1 + interval.max( 1 ),
					1 + interval.max( 0 ) );
			return address;
		};
		final BiConsumer< byte[], DirtyVolatileByteArray > copier = ( bytes, access ) ->
		{
			System.arraycopy( bytes, 0, access.getCurrentStorageArray(), 0, bytes.length );
			access.setDirty();
		};
		final HTTPLoader< DirtyVolatileByteArray > functor = new HTTPLoader<>( addressComposer, ( n ) -> new DirtyVolatileByteArray( ( int ) n, true ), copier );
		final IntervalKeyLoaderAsLongKeyLoader< DirtyVolatileByteArray > loader = new IntervalKeyLoaderAsLongKeyLoader<>( grid, functor );

		final DiskCachedCellImgOptions factoryOptions = options()
				.cacheType( CacheType.BOUNDED )
				.maxCacheSize( 1000 )
				.cellDimensions( cellDimensions );

		final Img< UnsignedByteType > httpImg = new DiskCachedCellImgFactory< UnsignedByteType >( factoryOptions )
				.createWithCacheLoader( dimensions, new UnsignedByteType(), loader );

		final BdvSource httpSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( httpImg, new SharedQueue( 20 ) ),
				"ilastik" );

		final Bdv bdv = httpSource;
		bdv.getBdvHandle().getViewerPanel().setDisplayMode( SINGLE );
		httpSource.setDisplayRange( 0.0, 255.0 );
	}
}
