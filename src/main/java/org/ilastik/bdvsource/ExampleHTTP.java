package org.ilastik.bdvsource;

import static bdv.viewer.DisplayMode.SINGLE;
import static net.imglib2.cache.img.DiskCachedCellImgOptions.options;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.DiskCachedCellImgOptions.CacheType;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.cache.util.IntervalKeyLoaderAsLongKeyLoader;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealFloatConverter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.volatiles.array.DirtyVolatileByteArray;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

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

		final RandomAccessible< FloatType > source = Converters.convert( Views.extendBorder( httpImg ), new RealFloatConverter<>(), new FloatType() );
		final CellLoader< FloatType > gradientLoader = new CellLoader< FloatType >()
		{
			@Override
			public void load( final SingleCellArrayImg< FloatType, ? > cell ) throws Exception
			{
				final int n = cell.numDimensions();
				for ( int d = 0; d < n; ++d )
				{
					final Img< FloatType > imgDim = ArrayImgs.floats( Intervals.dimensionsAsLongArray( cell ) );
					PartialDerivative.gradientCentralDifference2( Views.offsetInterval( source, cell ), imgDim, d );
					final Cursor< FloatType > c = imgDim.cursor();
					for ( final FloatType t : cell )
					{
						final float val = c.next().get();
						t.set( t.get() + val * val );
					}
				}
				for ( final FloatType t : cell )
					t.set( ( float ) Math.sqrt( t.get() ) );
			}
		};

		final Img< FloatType > gradientImg = new DiskCachedCellImgFactory< FloatType >( factoryOptions )
				.create( dimensions, new FloatType(), gradientLoader,
						options().initializeCellsAsDirty( true ) );

		final BdvSource httpSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( httpImg, new SharedQueue( 20 ) ),
				"ilastik" );

		final int numProc = Runtime.getRuntime().availableProcessors();
		final BdvSource gradientSource = BdvFunctions.show(
				VolatileViews.wrapAsVolatile( gradientImg, new SharedQueue( numProc - 1 ) ),
				"gradient",
				BdvOptions.options().addTo( httpSource ) );

		final Bdv bdv = httpSource;
		bdv.getBdvHandle().getViewerPanel().setDisplayMode( SINGLE );
		httpSource.setDisplayRange( 0.0, 255.0 );
		gradientSource.setDisplayRange( 0.0, 30.0 );
	}
}
