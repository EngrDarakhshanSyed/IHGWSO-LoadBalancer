package org.cloudbus.cloudsim.examples;
import org.cloudbus.cloudsim.examples.datacenter.*;

/*
 * Title:        LoadBalancer
 * Description:  A simulation to identify different approaches in Load Balancing of Cloud Computing
 * Authors:      Engr. Darakhshan Syed
 */

import java.util.*;


//import RoundRobin.RoundRobinDatacenterBroker;

import org.cloudbus.cloudsim.*;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import PSO.GenerateMatrices;
import PSO.PSO;


public class MainLoadBalancing {
//	// Duplicates bytes to two OutputStreams (console + file)
//	private static final class TeeOutputStream extends java.io.OutputStream {
//	    private final java.io.OutputStream a, b;
//	    TeeOutputStream(java.io.OutputStream a, java.io.OutputStream b) {
//	        this.a = a; this.b = b;
//	    }
//	    @Override public void write(int i) throws java.io.IOException { a.write(i); b.write(i); }
//	    @Override public void write(byte[] buf, int off, int len) throws java.io.IOException { a.write(buf, off, len); b.write(buf, off, len); }
//	    @Override public void flush() throws java.io.IOException { a.flush(); b.flush(); }
//	    @Override public void close() throws java.io.IOException { try { a.close(); } finally { b.close(); } }
//	}

	
	
	private static double[][] commMatrix;
    private static double[][] execMatrix;
    private static PSO PSOSchedularInstance;
    private static double mapping[];

    private static List<Vm> createVM(int userId, int numberOfVm) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> vmlist = new LinkedList<>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 3000; //1000;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        for(int i = 0; i< numberOfVm; i++){
        	long cap = mips+(i*100);
        	Log.printLine("VmId: " + i + " mips: " + cap);
            Vm vm = new Vm(i, userId, cap, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            vmlist.add(vm);
        }

        return vmlist;
    }

    private static List<Cloudlet> createCloudlet(int userId, int numberOfCloudlet){
        // Creates a container to store Cloudlets
        LinkedList<Cloudlet> list = new LinkedList<>();

        //cloudlet parameters
      //  long length = 1000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        long revert = numberOfCloudlet;
//        for(int i = 0; i < numberOfCloudlet; i++) {
//        	long cap = (length + 2L * (((int)(Math.random() * 10 ) + 1)) * 10);
//        	Log.printLine("cloudletId: " + i + " mips: " + cap);
//            Cloudlet cloudlet = new Cloudlet(i, cap, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
//            cloudlet.setUserId(userId);
//            list.add(cloudlet);
//            revert--;
//        }
        for (int i = 0; i < numberOfCloudlet; i++) {
            // Generate a random length between 1000 and 10000 MI (million instructions)
            long cap = 5000 + (long) (Math.random() * (10000 - 5000 + 1));
            Log.printLine("cloudletId: " + i + " length: " + cap + " mips");
            Cloudlet cloudlet = new Cloudlet(i, cap, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(userId);
            list.add(cloudlet);
            revert--;
        }

        return list;
    }

    public static void main(String[] args) {
    	
//    	// --- BEGIN: mirror console output to Downloads/load.txt ---
//    	try {
//    	    // Use your Downloads folder path
//    	    java.nio.file.Path outPath = java.nio.file.Paths.get(
//    	        "C:", "Users", "Darakhshan Syed", "Downloads", "load.txt"
//    	    );
//
//    	    java.io.OutputStream fileOut = new java.io.BufferedOutputStream(
//    	        new java.io.FileOutputStream(outPath.toFile(), true) // append = true
//    	    );
//
//    	    // Mirror STDOUT
//    	    java.io.PrintStream teeOut = new java.io.PrintStream(
//    	        new TeeOutputStream(System.out, fileOut)   // match constructor
//    	    );
//    	    System.setOut(teeOut);
//
//    	    // Mirror STDERR as well
//    	    java.io.PrintStream teeErr = new java.io.PrintStream(
//    	        new TeeOutputStream(System.err, fileOut)
//    	    );
//    	    System.setErr(teeErr);
//
//    	    System.out.println("[Log] Mirroring console to: " + outPath);
//    	} catch (Exception e) {
//    	    e.printStackTrace();
//    	}
//    	// --- END: mirror console output to Downloads/load.txt ---
//
//
//    	
//    	
//    	
//    	
    	
    	
        Log.printLine();
        Log.printLine("===================================== Load Balancer ==================================");
        Log.printLine("Title:        LoadBalancer" +
                "\nDescription:  A simulation to identify different approaches in Load Balancing of Cloud Computing" +
                "\nAuthor:      Engr. Darakhshan Syed");
        try {
            Calendar calendar = Calendar.getInstance();

            Scanner scanner = new Scanner(System.in);

            Log.printLine();
            Log.printLine("First step: Initialize the CloudSim package.");
            Log.printLine("Enter number of grid users:");
            int numUsers = scanner.nextInt();
            
            // Initialize the CloudSim library
            CloudSim.init(numUsers, calendar, false);

            Log.printLine();
            Log.printLine("Second step: Create Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation.");
            Log.printLine("Enter number of datacenters:");
            int numberOfDatacenters = scanner.nextInt();

            List<Datacenter> datacenterList = new LinkedList<Datacenter>();;
            for (int i = 0; i < numberOfDatacenters; i++) {
            	datacenterList.add(createDatacenter("Datacenter_" + i));
            }
          
            Log.printLine();
            Log.printLine("Third step: Create Cloudlets");
            Log.printLine("Enter number of cloudlet");
            int numberOfCloudlet = scanner.nextInt();
            
            Log.printLine();
            Log.printLine("Fourth step: Create Broker");
            Log.printLine("Select method for LoadBalancing:" +
                    "\n1.  Round Robin" +
                    "\n2.  Shortest Job First" +
                    "\n3.  First Come First Serve" +
                    "\n4.  Genetic Algorithm"+
                    "\n5.  Priority"+
                    "\n6.  IGWO"+
                    "\n7.  GWO Algo"
                    + "\n8.  PSOLoadBalancer"
                    + "\n9.  PSO"
                    + "\n10. GWSO"
                    + "\n11. HGWSO" + "\n12. HGA_GWO"+ "\n13. IHGWSO \n14. OptimizedHGWSO");

            DatacenterBroker broker = null;
        
            boolean gotBroker = false;
            
            

            while(!gotBroker) {
                int option = scanner.nextInt();
                try {
                    switch (option) {
                        case 1 :
                            broker = new RoundRobinDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        case 2 :
                            broker = new ShortestJobFirstDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        case 3 :
                            broker = new FCFSDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        case 4 :
                            broker = new GeneticAlgorithmDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        case 5 :
                            broker = new PriorityDatacenterBroker("Broker");
                            gotBroker = true;
                            break;
                        case 6 :
                            broker = new IIGWO("IIGWO_Broker");
                            gotBroker = true;
                            break;
                        case 7 :
                        	//broker = new TraditionalGWO("Broker");
                        	broker = new StandardGWOAlgorithm ("Broker");
                        	// broker = new GWOAlgorithm("Broker");
                        	//broker = new IGWO("Broker");
                        	//broker = new 	GWOAlgorithm("Broker");
                        
                            gotBroker = true;
                            break;
                        case 8 :
                            broker = new PSOLoadBalancer("Broker");
                        	//broker = new StandardPSO("Broker");	
                            gotBroker = true;
                            break;
                        case 9:
                        	new GenerateMatrices(numberOfCloudlet, datacenterList.size());
                            commMatrix = GenerateMatrices.getCommMatrix();
                            execMatrix = GenerateMatrices.getExecMatrix();
                            PSOSchedularInstance = new PSO(numberOfCloudlet, datacenterList.size());
                            mapping = PSOSchedularInstance.run();
                            
                            HashSet<Integer> dcIds = new HashSet<>();
                            HashMap<Integer, Integer> hm = new HashMap<>();
                            
                            for (Datacenter dc : datacenterList) {
                                if (!dcIds.contains(dc.getId()))
                                    dcIds.add(dc.getId());
                            }
                            Iterator<Integer> it = dcIds.iterator();
                            for (int i = 0; i < mapping.length; i++) {
                                if (hm.containsKey((int) mapping[i])) continue;
                                hm.put((int) mapping[i], it.next());
                            }
                            for (int i = 0; i < mapping.length; i++)
                                mapping[i] = hm.containsKey((int) mapping[i]) ? hm.get((int) mapping[i]) : mapping[i];
                            
                            broker = new PSODatacenterBroker("Broker", mapping);
                            gotBroker = true;
                        	break;
                        case 10:
                        	broker = new GWSO("Broker");
                            gotBroker = true;
                            break;
                        case 11:
                        	broker = new HGWSO("Broker");
                            gotBroker = true;
                            break;
	                    case 12:
	                    	broker = new GA_GWO("Broker");
                            gotBroker = true;
                           break;
	                    case 13:
	                    	broker = new IHGWSO("Broker");
	                    	//broker = new HGWSO ("Broker");
                            gotBroker = true;
                           break; 
	                    case 14:
	                    	broker = new OptimizedHGWSO("Broker");
	                    	gotBroker = true;
	                    	break;
                        default:
                            Log.printLine("Please, select from [1-] only:");
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int brokerId = broker.getId();

            Log.printLine();
            Log.printLine("Fifth step: Create VMs");
            Log.printLine("Enter number of vms:");
            int numberOfVm = scanner.nextInt();

            List<Vm> vmList = createVM(brokerId, numberOfVm); 

            List<Cloudlet> cloudletList = createCloudlet(brokerId, numberOfCloudlet);
            Log.printLine("Sending them to broker...");
            

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            Log.printLine();
            Log.printLine("Sixth step: Starts the simulation");
            Log.printLine("Press any key to continue...");
            scanner.next();

            CloudSim.startSimulation();

            Log.printLine();
            Log.printLine("Final step: Print results when simulation is over");
            Log.printLine("Press any key to continue...");
            scanner.next();

            List<Cloudlet> cloudletReceivedList = broker.getCloudletReceivedList();
            List<Vm> vmsCreatedList = broker.getVmsCreatedList();
 
            CloudSim.stopSimulation();
            
            List<Cloudlet> finishedCloudlets = broker.getCloudletReceivedList();
            //calculateMetrics(finishedCloudlets);


            printCloudletList(cloudletReceivedList, vmList);

            Log.printLine();
            Log.printLine("Simulation Complete");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name){

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more
        //    Machines
        List<Host> hostList = new ArrayList<>();

        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
        //    create a list to store these PEs before creating
        //    a Machine.
        List<Pe> peList1 = new ArrayList<>();

        int mips = 10000;

        // 3. Create PEs and add these into the list.
        //for a quad-core machine, a list of 4 PEs is required:
        peList1.add(new Pe(0, new PeProvisionerSimple(mips + 500))); // need to store Pe id and MIPS Rating
        peList1.add(new Pe(1, new PeProvisionerSimple(mips + 1000)));
        peList1.add(new Pe(2, new PeProvisionerSimple(mips + 1500)));
        peList1.add(new Pe(3, new PeProvisionerSimple(mips + 700)));
        
        Log.printLine(name + "  Host1 mips: " + ( (mips + 500)+(mips + 1000)+(mips + 1500)+(mips + 700) ));

        //Another list, for a dual-core machine
        List<Pe> peList2 = new ArrayList<>();

        peList2.add(new Pe(0, new PeProvisionerSimple(mips + 700)));
        peList2.add(new Pe(1, new PeProvisionerSimple(mips + 900)));
        
        Log.printLine(name + "  Host2 mips: " + ((mips + 900)+(mips + 700) ));

        
        
        
        peList1.add(new Pe(0, new PeProvisionerSimple(mips + 500))); // need to store Pe id and MIPS Rating
        peList1.add(new Pe(1, new PeProvisionerSimple(mips + 1000)));
        peList1.add(new Pe(2, new PeProvisionerSimple(mips + 1500)));
        peList1.add(new Pe(3, new PeProvisionerSimple(mips + 700)));
        
        
        Log.printLine(name + "  Host3 mips: " + ( (mips + 500)+(mips + 1000)+(mips + 1500)+(mips + 700) ));

        peList2.add(new Pe(0, new PeProvisionerSimple(mips + 700)));
        peList2.add(new Pe(1, new PeProvisionerSimple(mips + 900)));
        Log.printLine(name + "  Host4 mips: " + ((mips + 900)+(mips + 7000)));
        
        //4. Create Hosts with its id and list of PEs and add them to the list of machines
        int hostId=0;
        int ram = 1002048; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;
        int NUMBER_OF_HOST = 2;
        
        for (hostId = 1; hostId <= NUMBER_OF_HOST; hostId++)
        {
        	hostList.add(
                    new Host(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bw),
                            storage,
                            peList1,
                            new VmSchedulerTimeShared(peList1)
                    )
            ); // This is our first machine
        }

        
//        hostList.add(
//                new Host(
//                        hostId,
//                        new RamProvisionerSimple(ram),
//                        new BwProvisionerSimple(bw),
//                        storage,
//                        peList2,
//                        new VmSchedulerTimeShared(peList2)
//                )
//        ); // Second machine


        //To create a host with a space-shared allocation policy for PEs to VMs:
        //hostList.add(
        //		new Host(
        //			hostId,
        //			new CpuProvisionerSimple(peList1),
        //			new RamProvisionerSimple(ram),
        //			new BwProvisionerSimple(bw),
        //			storage,
        //			new VmSchedulerSpaceShared(peList1)
        //		)
        //	);

        //To create a host with an opportunistic space-shared allocation policy for PEs to VMs:
        //hostList.add(
        //		new Host(
        //			hostId,
        //			new CpuProvisionerSimple(peList1),
        //			new RamProvisionerSimple(ram),
        //			new BwProvisionerSimple(bw),
        //			storage,
        //			new VmSchedulerOpportunisticSpaceShared(peList1)
        //		)
        //	);


        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.1;	// the cost of using storage in this resource
        double costPerBw = 0.1;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    /**
     * Prints the Cloudlet objects
     * @param list  list of Cloudlets
     */

//    private static void printCloudletList(List<Cloudlet> list, List<Vm> vmList) {
//    
//    	
//         double totalFinishedCloudlets = 0;
//         double totalExecTime = 0;
//         double makespan = 0;
//         
//         double resource_utilization_cpu = 0;
//         double totalCompletionTime=0;
//         double totalCost=0;
//         double totalWaitingTime=0;
//        String indent = "    ";
//        Log.printLine();
//        Log.printLine();
//        Log.printLine("========================================== OUTPUT ==========================================");
//        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
//                "Datacenter ID" + indent + "VM ID" + indent + " " + "Time" + indent + "Start Time" + indent + "Finish Time");
//        double time = 0;
//        double resTime = 0;
//        double compTsk = 0;
//
//        for (Cloudlet value : list) {
//            Log.print(indent + String.format("%02d", value.getCloudletId()) + indent + indent);
//
//            if (value.getCloudletStatus() == Cloudlet.SUCCESS) {
//                Log.print("SUCCESS");
//                
//                double completionTime= value.getActualCPUTime()+ value.getWaitingTime();
//                double cost= value.getCostPerSec()* value.getActualCPUTime() ;
//                
//                time += (  value.getFinishTime() - value.getExecStartTime());
//                resTime += value.getFinishTime() - value.getSubmissionTime();
//              //  resTime += (time-value.getActualCPUTime());
//                compTsk += 1;
//                Vm vm = vmList.get(value.getVmId());
//                
//                
//                resource_utilization_cpu += calculateResourceUtilization(value, vm);
//                
//                
//                totalExecTime += value.getActualCPUTime();
//                
//                if (value.getStatus() == Cloudlet.SUCCESS) {
//                    totalFinishedCloudlets++;
//                }
//                
//                double finishTime = value.getFinishTime();
//                if (finishTime > makespan) {
//                	makespan = finishTime;
//                }
//                
//                totalCompletionTime += completionTime;
//                totalCost += cost;
//                totalWaitingTime+=value.getWaitingTime();
//                
//                Log.printLine(indent + indent + String.format("%02d", value.getResourceId()) +
//                        indent + indent + indent + String.format("%02d", value.getVmId()) +
//                        indent + indent + String.format("%.2f", value.getActualCPUTime()) +
//                        indent + indent + String.format("%.2f", value.getExecStartTime()) +
//                        indent + indent + indent + String.format("%.2f", value.getFinishTime()));
//            }
//        }
//
//        double avgTime = time/list.toArray().length;
//        double avgResTime = resTime/list.toArray().length;
//     //   double rUtl = (avgResTime/time)*100;
//     //-------------------------
//       // String resUtilization = String.format("%.2f", rUtl);
//        double throughPut = totalFinishedCloudlets/makespan;
//        
//     //   double resourceUtilization = calculateTotalResourceUtilization(vmList, list, makespan);
//  
//        double scalability = totalFinishedCloudlets / list.size();
//  
//        Log.printLine("Total CPU Time : " + time);
//        Log.printLine("Average CPU time: " + avgTime);
//        Log.printLine("Makespan: " + makespan);
//        Log.printLine("Response Time: " + resTime);
//        Log.printLine("Average Response Time: " + avgResTime);
//      //  Log.printLine("Resource Utilization: " + resUtilization + "%"); //in terms of CPU
//        Log.printLine("Throughput: " + throughPut + "cloudlets/sec");
//        
//        //my parameters
//       
//        Log.printLine("Total Execution Time: " + totalExecTime);
//     //   Log.printLine("Resource Utilization: " + resourceUtilization);
//        
//        System.out.println("Scalability: " + scalability);
//        System.out.println("totalCompletionTime : " + totalCompletionTime );
//        System.out.println("totalCost : " + totalCost + "in running the cloudlets per second" );  
//        System.out.println("totalWaitingTime : " + totalWaitingTime );
//      //  Log.printLine("Total Resource Utilization across all VMs: " + resourceUtilization + "%");
//
//        Log.printLine("Resource Utilization: " + (resource_utilization_cpu/list.size()) + "%"); //in terms of CPU
//
//    }
//    public static double calculateResourceUtilization(Cloudlet cloudlet, Vm vm) {
//        double cloudletLength = cloudlet.getCloudletLength(); // in MI
//        double vmMIPS = vm.getMips(); // VM's MIPS rating
//        double cloudletExecutionTime = cloudlet.getActualCPUTime(); // in seconds
//
//        // Calculate resource utilization percentage
//        double resourceUtilization = (cloudletLength / (vmMIPS * cloudletExecutionTime)) * 100.0;
//        
//        return resourceUtilization;
//    }
//    
//    
////    public static double calculateTotalResourceUtilization(List<Vm> vmList, List<Cloudlet> cloudletList, double time) {
////        double totalVmCapacity = 0; // Total available MIPS across all VMs
////        double totalUsedMips = 0;   // Total MIPS consumed by cloudlets
////
////        // Calculate the total capacity (MIPS) of all VMs
////        for (Vm vm : vmList) {
////            totalVmCapacity += vm.getMips(); // Add each VM's MIPS rating
////        }
////
////        // Calculate the total MIPS consumed by cloudlets across all VMs
////        for (Cloudlet cloudlet : cloudletList) {
////            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
////                double cloudletLength = cloudlet.getCloudletLength(); // Cloudlet length in MI
////                double cloudletExecutionTime = cloudlet.getActualCPUTime(); // Cloudlet execution time in seconds
////                Vm vm = vmList.get(cloudlet.getVmId()); // Get the VM that executed this cloudlet
////                double vmMips = vm.getMips(); // MIPS rating of the VM
////
////                double mymips = cloudletLength / cloudletExecutionTime;
////                totalUsedMips += mymips; // MIPS used by this cloudlet
////            }
////        }
////
////        // Calculate the resource utilization percentage across all VMs
////        double resourceUtilization = (totalUsedMips / totalVmCapacity) * 100.0;
////
////        return resourceUtilization;
////    }
//
//}
    private static void printCloudletList(List<Cloudlet> list, List<Vm> vmList) { 
        double totalFinishedCloudlets = 0;
        double totalExecTime = 0;
        double makespan = 0;
        double totalCompletionTime = 0;
        double totalCost = 0;
        double totalWaitingTime = 0;
        Map<Integer, Double> vmExecutionTimes = new HashMap<>(); // Store execution time per VM
        
        String indent = "    ";
        Log.printLine();
        Log.printLine("========================================== OUTPUT ==========================================");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Datacenter ID" + indent + "VM ID" + indent + " " + 
                       "Time" + indent + "Start Time" + indent + "Finish Time");

        double time = 0;
        double resTime = 0;
        double compTsk = 0;

        // Initialize the execution time for each VM to 0
        for (Vm vm : vmList) {
            vmExecutionTimes.put(vm.getId(), 0.0);
        }

        // First, calculate execution times for VMs that executed cloudlets
        for (Cloudlet value : list) 
        {
            Log.print(indent + String.format("%02d", value.getCloudletId()) + indent + indent);

            if (value.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                double completionTime = value.getActualCPUTime() + value.getWaitingTime();
                double cost = value.getCostPerSec() * value.getActualCPUTime();
                
                time += (value.getFinishTime() - value.getExecStartTime());
                resTime += value.getFinishTime() - value.getSubmissionTime();
                compTsk += 1;

                Vm vm = vmList.get(value.getVmId());
                
                // Add the cloudlet's actual execution time to the VM's total execution time
                vmExecutionTimes.put(vm.getId(), vmExecutionTimes.get(vm.getId()) + value.getActualCPUTime());
                
                totalExecTime += value.getActualCPUTime();
                
                if (value.getStatus() == Cloudlet.SUCCESS) {
                    totalFinishedCloudlets++;
                }

                double finishTime = value.getFinishTime();
                if (finishTime > makespan) {
                    makespan = finishTime;
                }
                
                totalCompletionTime += completionTime;
                totalCost += cost;
                totalWaitingTime += value.getWaitingTime();
                
                Log.printLine(indent + indent + String.format("%02d", value.getResourceId()) +
                              indent + indent + indent + String.format("%02d", value.getVmId()) +
                              indent + indent + String.format("%.2f", value.getActualCPUTime()) +
                              indent + indent + String.format("%.2f", value.getExecStartTime()) +
                              indent + indent + indent + String.format("%.2f", value.getFinishTime()));
            }
        }

        double avgTime = time / list.size();
        double avgResTime = resTime / list.size();
        double throughPut = totalFinishedCloudlets / makespan;
        
        double scalability = totalFinishedCloudlets / list.size();
        
        Log.printLine("Total CPU Time : " + time);
        Log.printLine("Average CPU time: " + avgTime);
        Log.printLine("Makespan: " + makespan);
        Log.printLine("Response Time: " + resTime);
        Log.printLine("Average Response Time: " + avgResTime);
        Log.printLine("Throughput: " + throughPut + " cloudlets/sec");
        
        Log.printLine("Total Execution Time: " + totalExecTime);
       // Log.printLine("Scalability: " + scalability);
        Log.printLine("Total Completion Time: " + totalCompletionTime);
        Log.printLine("Total Cost: " + totalCost + " (in running cloudlets per second)");
        Log.printLine("Total Waiting Time: " + totalWaitingTime);

        // Now, calculate the resource utilization for all VMs based on execution time and makespan
        double totalVmUtilization = 0;
        double maxUtilization = Double.MIN_VALUE;
        double minUtilization = Double.MAX_VALUE;
        List<Double> vmUtilizations = new ArrayList<>();
        
        for (Vm vm : vmList) {
            double vmExecutionTime = vmExecutionTimes.get(vm.getId());
            double vmUtilization = (vmExecutionTime / makespan) * 100.0; // Utilization as a percentage

            totalVmUtilization += vmUtilization;
            vmUtilizations.add(vmUtilization);
            if (vmUtilization > maxUtilization)
            {
            	maxUtilization = vmUtilization;
            }
            if (vmUtilization < minUtilization)
            {
            	minUtilization = vmUtilization;
            }
            //Log.printLine("VM ID: " + vm.getId() + " Utilization: " + vmUtilization + "%");
        }

        // Average resource utilization across all VMs
        double avgVmUtilization = totalVmUtilization / vmList.size();
       // double degreeOfImbalance = (maxUtilization - minUtilization) / avgVmUtilization;
        double maxExecutionTime = Collections.max(vmExecutionTimes.values());
        double minExecutionTime = Collections.min(vmExecutionTimes.values());
        double avgExecutionTime = totalExecTime / vmList.size();
        double degreeOfImbalance = ((maxExecutionTime - minExecutionTime) / avgExecutionTime)/vmList.size();
        // Print the overall resource utilization considering all VMs
       //"Overall Average Resource Utilization across all VMs 
        Log.printLine("Average RU: " + avgVmUtilization + "%");
         Log.printLine("Degree of Imbalance (DoI): " + degreeOfImbalance);
    }
}
    
    
    
