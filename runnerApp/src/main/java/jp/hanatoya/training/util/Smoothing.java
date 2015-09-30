package jp.hanatoya.training.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * Implemented from http://www.hindawi.com/journals/ijap/2014/372814/
 */
public class Smoothing {
	public static enum FilterType {THREE_POINT_AVERAGE, HANNING, HANNING_RECURSIVE, FIVE_POINT_WEIGHTED_AVERAGE, FIVE_POINT_TRIPLE};
	
	private LinkedList<Double> cache1, cache2, cache3, cache4, cache5;
	private FilterType type;
	
	public Smoothing() {
		this.cache1 = new LinkedList<Double>();
		this.cache2 = new LinkedList<Double>();
		this.cache3 = new LinkedList<Double>();
		this.cache4 = new LinkedList<Double>();
		this.cache5 = new LinkedList<Double>();
	}
	
	public Smoothing(FilterType filterType){
		this();
		this.type = filterType;
	}
	

	public synchronized Double[] oneFilter(double val){
		switch (type) {
		case THREE_POINT_AVERAGE:
			return threePointAverage(val);	
		case FIVE_POINT_TRIPLE:
			return fivePointTriple(val);
		case FIVE_POINT_WEIGHTED_AVERAGE:
			return fivePointWeightedAverage(val);
		case HANNING:
			return hanning(val);
		case HANNING_RECURSIVE:
			 return hanningRecursive(val);
		default:
			return null;
		}
		
	}
	
	public synchronized ArrayList<Double[]> on (double val){
		ArrayList<Double[]> res = new ArrayList<Double[]>();
		res.add(threePointAverage(val));  //blue
		res.add(fivePointTriple(val)); // white
		res.add(fivePointWeightedAverage(val)); // green
		res.add(hanning(val)); //yellow
		res.add(hanningRecursive(val)); //red
		return res;
	}
	
	private Double[] threePointAverage(double val){
		Double[] res = new Double[1];
		cache1.addFirst(val);
			switch (cache1.size()){
			case 2 : res[0] = (2 * cache1.get(1) + cache1.get(0)) / 3;
			break;
			case 3 : res[0] = (cache1.get(0) + cache1.get(1) + cache1.get(2)) / 3;
			cache1.removeLast();
					break;
			default : res = null;
			break;
		}
		return res;
	
	}
	
	private Double[] hanning(double val){
		Double[] res = new Double[1];
		cache2.addFirst(val);
		switch (cache2.size()){
		
		case 3 : res[0] = (cache2.get(2) + 2 * cache2.get(1) + cache2.get(0)) / 4;
				cache2.removeLast();
				 break;
		
		case 2 : 
		case 1 :
		default : res[0] = 0.0d;
		break;		
		}
		return res;
		
	}
	
	private Double[] hanningRecursive(double val){
		Double[] res = new Double[1];

		switch (cache3.size()){
		case 0 : 
			cache3.addFirst(val); // in this case, cache stores y
			res[0] = val;
			break;
		case 1 : 
			res[0] = (val + cache3.getFirst()) / 2;
			cache3.addFirst(val);
			break;
		default :
			res[0] = (val + 2 * cache3.getFirst() + cache3.getLast()) / 4;
			cache3.removeLast();
			cache3.addFirst(val);
			break;
		}
		return res;
	}
	
	private Double[] fivePointWeightedAverage(double val){
		Double[] res = new Double[2];
		switch (cache4.size()) {
		case 0:
		case 1:
		case 2: 
			cache4.addFirst(val);
			return null;
		case 3:
			cache4.addFirst(val);
			res[0] = (3 * cache4.getLast() + 2 * cache4.get(2) + cache4.get(1) - cache4.get(0)) / 5;
			res[1] = (4 * cache4.getLast() + 3 * cache4.get(2) + 2 * cache4.get(1) + cache4.get(0)) / 10;
			break;
		default :
			res[0] = 0.0d;
			cache4.addFirst(val);
			for(Double d : cache4){
				res[0] += d;
			}
			res[0] += val;
			res[0] = res[0] / 5;
			cache4.removeLast();
			break;
		}
		return res;	
	}
	
	
	private Double[] fivePointTriple(double val){
		Double[] res = new Double[2];
		switch (cache5.size()) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4: cache5.addFirst(val);
		break;
		case 5:
			cache5.addFirst(val);
			res[0] = (69 * cache5.get(4) + 4 * cache5.get(3) - 6 * cache5.get(2) + 4 * cache5.get(1) - cache5.get(0)) / 70;
			res[1] = (2 * cache5.get(4) + 27 * cache5.get(3) + 12 * cache5.get(2) - 8 * cache5.get(1) + 2 * cache5.get(0)) / 35;
			cache5.removeLast();
			break;

		default: 
			cache5.addFirst(val);
			res[0] = (-3 * cache5.get(4) + 12 * cache5.get(3) + 17 * cache5.get(2) + 12 * cache5.get(1) - 3 *cache5.get(0)) / 35;
			cache5.removeLast();
			break;
		}
		
		return res;
		
	}
	
	
	
	
//	// use kernel generator http://www.embege.com/gauss/
//	private double[] kernelGauss = {		
//			0.05448868454964433, 
//			0.24420134200323346, 
//			0.40261994689424435, 
//			0.24420134200323346, 
//			0.05448868454964433, 
//	};
//	
//	private void movingAverage(double power){
//		filter.removeLast();
//		filter.addFirst(power);
//		
//		double filterSum = 0.0d;
//		// Apply convolution here
//		for (int i = 0; i < SIZE_FILTER; i++ ){
//			filter.set(i, filter.get(i) * kernelGauss[i]);
//			filterSum += filter.get(i);		
//		}
//		
//		
//		smoothed = filterSum / SIZE_FILTER;
//		
//	}
}
