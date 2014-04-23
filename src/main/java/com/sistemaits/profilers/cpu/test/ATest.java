package com.sistemaits.profilers.cpu.test;

import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class ATest {

	public static void main(String[] args) {
		
		System.out.println("*** Ciao! ***");
		
		System.out.println(Cpu.class.getCanonicalName());
		
		Sigar sigar = new Sigar();
		
		try {
			System.out.println(sigar.getFQDN());
			
			CpuInfo cpu = sigar.getCpuInfoList()[0];
			
			System.out.println(cpu.getCacheSize());
			System.out.println(cpu.getVendor());
			System.out.println(cpu.getMhz());
			System.out.println(cpu.getTotalSockets());
		} 
		catch (SigarException e) {
			e.printStackTrace();
		}
		
	}
	
}
