package com.sistemaits.profilers.cpu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * Keeps the sizes as byte
 * @author tommaso.doninelli
 *
 */
public class MemoryTest {

	/*
	 * TODO
	 * 	- Print specs about CPU (cache, TLB, vendor, etc.)
	 * 	- Add help if no args are passed in
	 * 	- Add params to run all all test
	 */
	
	public static final int LONG_SIZE = 8;
	public static final int PAGE_SIZE = 2 * 1024 * 1024;	// Standard page is 2kb
	public static final int ONE_GIG = 1024 * 1024 * 1024;
	public static final long TWO_GIG = 2L * ONE_GIG;
	
	public static final int ARRAY_SIZE = (int) (TWO_GIG/LONG_SIZE); // The number of long to have a 2G array
	public static final int WORDS_PER_PAGE = PAGE_SIZE / LONG_SIZE;	// How many long in a memory page?
	
	public static final int ARRAY_MASK = ARRAY_SIZE -1;		// ???
	public static final int PAGE_MASK = WORDS_PER_PAGE - 1;		// ???
	
	public static final int PRIME_INC = 514229; // ???
	
	public static final long[] memory = new long[ARRAY_SIZE];	
	
	// Fill our memory
	static {
		for(int i=0; i< ARRAY_SIZE; i++){
			memory[i] = 777;
		}
	}
	
	public enum StrideType {
		
		VERY_LINEAR_WALK {
			public int next(final int pageOffset, final int wordOffset, final int pos){
				return pos + 1;
			}
		},
		
		LINEAR_WALK {
			public int next(final int pageOffset, final int wordOffset, final int pos){
				return (pos + 1) & ARRAY_MASK;
			}
		},
		
		RANDOM_PAGE_WALK {
			public int next(final int pageOffset, final int wordOffset, final int pos){
				return pageOffset + ((pos + PRIME_INC) & PAGE_MASK);
			}
		},
		
		RANDOM_HEAP_WALK {
			public int next(final int pageOffset, final int wordOffset, final int pos){
				return (pos + PRIME_INC) & ARRAY_MASK;
			}
		};
		
		public abstract int next(int pageOffset, int wordOffset, int pos);
	}
	
	private static void doTest(final int runNumber, final StrideType strideType) {
		
		final long startTime = System.nanoTime();
		
		int pos = -1;
		long result = 0;
		
		// For each page in memory
		for (int pageOffset = 0; pageOffset < ARRAY_SIZE; pageOffset += WORDS_PER_PAGE) {
			
			// For each word in a page
			for(int wordOffset = pageOffset, limit = pageOffset + WORDS_PER_PAGE; wordOffset<limit; wordOffset++) {

				// Read the "next" element; next depends on the pattern we are testing
				pos = strideType.next(pageOffset, wordOffset, pos);	
				result += memory[pos];								
			}
			
		}
		
		final long duration = System.nanoTime() - startTime;
		final double nsOp = duration / (double)ARRAY_SIZE;
		if (208574349312L != result) {
			throw new IllegalStateException();
		}
		
		String res = String.format("%d - %.2fns %s\n",
				Integer.valueOf(runNumber),
				Double.valueOf(nsOp),
				strideType);
		
		System.out.println(res);
		flushToFile(res);
	}
	
	
	private static void flushToFile(String s){
		
		File logFile=new File("results.txt");
		
	    try(BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
	    	writer.write (s);
	    } catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static void main(String[] args) {
		int type = -1;
		if(args != null && args.length > 0){
			type = Integer.parseInt(args[0]);
		}
		
		final StrideType stride = StrideType.values()[type];
		
		System.out.println("Welcome! After gathered some info about the hardware, all test will be performed.");
		System.out.println("The results are copied in results.txt");
		
		
		String cpuInfo = "";
		try {
			cpuInfo = gatherCpuInfo();
		} 
		catch (SigarException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println(cpuInfo);
		flushToFile(cpuInfo);
		
		for(int i=0; i<5; i++){
			doTest(i, stride);
		}
	}


	private static String gatherCpuInfo() throws SigarException {

		Sigar sigar = new Sigar();
		sb = new StringBuilder();
		
		write("* * System information * *");
		write("HOST: " + sigar.getFQDN());
		write("MEMORY");
		
//		Mem mem = sigar.getMem();
//		write("  Total: " + mem.getTotal());
//		write("  Free: " + mem.getFree());
		write("\nCPU");
		
		CpuInfo[] cpus = sigar.getCpuInfoList();
		for(CpuInfo cpu : cpus){
			write("  " + cpu.getVendor() + cpu.getModel());
			write("  MHz: " + cpu.getMhz());
			write("  Cache: " + cpu.getCacheSize());
			write("----");
		}
		write("");
		
		return sb.toString();
	}
	
	private static StringBuilder sb = null;
	private static void write(String message){
		sb.append(message).append("\n");
	}
}

