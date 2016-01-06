package uq.truster;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;

import uq.fs.FileToObjectRDDService;
import uq.spark.EnvironmentVariables;
import uq.spatial.Grid;
import uq.spatial.Point;
import uq.spatial.Trajectory;
import uq.truster.partition.Partition;
import uq.truster.partition.SpatialPartitionModule;

/**
 * Truster main app class
 * 
 * @author uqdalves
 *
 */
public class TrusterApp implements EnvironmentVariables {
	// grid/space dimensions
	private final static double MIN_X = 50.0;  // MinX: 52.99205499607079
	private final static double MIN_Y = -25.0; // MinY: -20.08557496216634
	private final static double MAX_X = 720.0; // MaxX: 716.4193496072005
	private final static double MAX_Y = 400.0; // MaxY: 395.5344310979076
	// number of grid partitions
	private final static int SIZE_X = 32;
	private final static int SIZE_Y = 32;
	
	/**
	 * Spark main access
	 */
	public static void main(String[] arg){
		/*****
		 * READ DATA AND CONVERT TO TRAJECTORIES 
		 *****/
		JavaRDD<Trajectory> trajectoryRDD = readData();
		
		/*****
		 * CREATE A GRID FOR PARTITIONING THE DATA 
		 *****/
		Grid grid = new Grid(SIZE_X, SIZE_Y, MIN_X, MIN_Y, MAX_X, MAX_Y);
		
		/*****
		 * PARTITION THE DATA - TRUSTER SPATIAL PARTITION MODULE 
		 *****/
		SpatialPartitionModule partitionMod = new SpatialPartitionModule();
		JavaPairRDD<Integer, Partition> partitionsRDD = 
				partitionMod.partition(trajectoryRDD, grid);
		
		// force action to build partitions
		System.out.println("Num. Partitions: " + partitionsRDD.count());
		
		/*****
		 * PROCESS QUERIES - TRUSTER QUERY PROCESSING MODULE 
		 *****/
		// TODO: process queries using the partitionRDD
	}
	
	/**
	 * Read input dataset and convert to a RDD of trajectories
	 */
	public static JavaRDD<Trajectory> readData(){
		System.out.println("Reading data..");
		
		// read raw data to Spark RDD
		JavaRDD<String> fileRDD = SC.textFile(DATA_PATH);
		fileRDD.persist(STORAGE_LEVEL);
		
		// convert the input data to a RDD of trajectory objects
		FileToObjectRDDService rdd = new FileToObjectRDDService();
		JavaRDD<Trajectory> trajectoryRDD = 
				rdd.mapRawDataToTrajectoryRDD(fileRDD);
		trajectoryRDD.persist(STORAGE_LEVEL);
		
		return trajectoryRDD;
	}

	/**
	 * Get min and max coodinate points (x,y) in the dataset
	 * @param trajectoryRDD
	 */
	/*public static double[] getMinMax(JavaRDD<Trajectory> trajectoryRDD){
		double[] minMax = 
			trajectoryRDD.map(new Function<Trajectory, double[]>() {
				public double[] call(Trajectory t) throws Exception {
					double minX = Double.MAX_VALUE;  
					double minY = Double.MAX_VALUE;  
					double maxX = -Double.MAX_VALUE;  
					double maxY = -Double.MAX_VALUE;
					// get min max x and y
					for(Point p : t.getPointsList()){
						if(p.x < minX) minX = p.x;
						if(p.y < minY) minY = p.y;
						if(p.x > maxX) maxX = p.x;
						if(p.y > maxY) maxY = p.y;
					}
					double[] result = new double[]{minX,minY,maxX,maxY};
					return result;
				}
			}).reduce(new Function2<double[], double[], double[]>() {
				public double[] call(double[] v1, double[] v2) throws Exception {
					v1[0] = Math.min(v1[0], v2[0]);
					v1[1] = Math.min(v1[1], v2[1]);
					v1[2] = Math.max(v1[2], v2[2]);
					v1[3] = Math.max(v1[3], v2[3]);
					return v1;
				}
			});
		return minMax;
	}*/
}