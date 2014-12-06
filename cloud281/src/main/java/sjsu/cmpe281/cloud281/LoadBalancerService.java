package sjsu.cmpe281.cloud281;

import static spark.Spark.get;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sjsu.cmpe281.requestAndResource.LocationBased;
import sjsu.cmpe281.requestAndResource.RequestResourceStorage;
import sjsu.cmpe281.requestAndResource.ResourceRequest;
import sjsu.cmpe281.requestAndResource.ResourceStorage;
import sjsu.cmpe281.requestAndResource.Resources;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

public class LoadBalancerService {

	public static void main(String[] args) throws IOException {

		ResourceStorage resourceStorage = new ResourceStorage();
		RequestResourceStorage requestResourceStorage = new RequestResourceStorage();
		LocationBased locationBased = new LocationBased();
		Random rand = new Random();
		int count = 1000;

		// Populating the Back-end Resource Store depending on resource ID.
		for (int i = 0; i < count / 2; i++) {

			String resourceName = "EC2_" + i;
			Resources resources = new Resources();
			resources.setResourceName(resourceName);
			resources.setLocationId(rand.nextInt(5) +1);
			resources.setCpu_units(rand.nextInt(10) +1);
			resources.setMemory(rand.nextInt(100) +1);
			resources.setStorage(rand.nextInt(200) +1);
			resources.setFullAllocation(false);
			resources.setPartialAllocation(false);

			resourceStorage.addResourceToHashMap(resourceName, resources);
			
			// Populating the back-end Resource Store depending on location ID.
			locationBased.putLocationBasedAllocatedResources(resources);
		}

		// Web Service Generating 1000 Requests with REQUEST-ID
		get("/request",
				(request, response) -> {

					int requestID;
					int requestLocationID;
					ArrayList<Integer> requestIDList = new ArrayList<Integer>();
					
					String heading = "********************Generated 1000 RequestIDs are as follows**********************";
					String template = heading + System.lineSeparator();

					for (int i = 0; i < count; i++) {
							requestID = 5000+i+1;
							requestLocationID = rand.nextInt(5) + 1;
							ResourceRequest resourceRequest = new ResourceRequest();
							resourceRequest.setRequestId(requestID);
							resourceRequest.setLocationId(requestLocationID);
							resourceRequest.setMemory(rand.nextInt(100) +1);
							resourceRequest.setCpu_units(rand.nextInt(10) +1);
							resourceRequest.setStorage(rand.nextInt(200) +1);
							resourceRequest.setRequest_description("Gaming Application");
							resourceRequest.setAllocated(false);
	
							requestResourceStorage.addRequestsToHashMap(requestID,resourceRequest);
							requestIDList.add(requestID);
							
							System.out.println("**********************************************************************************");
							System.out.println("Number of Requests: " +i);
							System.out.println("Generated Request ID: " +requestID);
							System.out.println("Generated Location ID: " +requestLocationID);
							System.out.println("Generated Request Description: " +resourceRequest.getRequest_description());
							System.out.println("Generated Request CPU units: " +resourceRequest.getCpu_units()+"mHZ");
							System.out.println("Generated Request Memory units: " +resourceRequest.getMemory()+"MB");
							System.out.println("Generated Request Storage units: " +resourceRequest.getStorage()+"GB");
							System.out.println("**********************************************************************************");
					}
					
					
					Map<String, Object> requestTemplate = new HashMap<String, Object>();
					requestTemplate.put("request-ID: ", requestIDList.get(0).toString());

					return template
							+ requestResourceStorage.getRequestsFromHashMap();
				});
					/*return new ModelAndView(requestTemplate, "request.ftl");
				}, new FreeMarkerEngine());*/
		
		// Home page
		get("/home",(request, response) -> {
			Map<String, Object> homeTemplate = new HashMap<String, Object>();
			homeTemplate.put("message", "Welcome to Laod Balancing Algorithms");
		
			return new ModelAndView(homeTemplate, "home.ftl");
			}, new FreeMarkerEngine());
				
		// Resource Allocation is done by Ant Colony Algorithm
		get("/antColonyAllocation",(request, response) -> {

					ArrayList<Resources> resourceList = new ArrayList<Resources>(resourceStorage.getResourcesFromHashMap());
					ArrayList<ResourceRequest> requestList = new ArrayList<ResourceRequest>(requestResourceStorage.getResourcesFromHashMap());
					ArrayList<String> resourceNames = new ArrayList<String>();

					for (int j = 0; j < resourceList.size(); j++) {
						String requestName = resourceList.get(j).getResourceName();
						int cpu = resourceList.get(j).getCpu_units();
						int memory = resourceList.get(j).getMemory();
						int storage = resourceList.get(j).getStorage();
						int location = resourceList.get(j).getLocationId();
						System.out.println("**********************************************************");
						System.out.println("### Resources Before Allocation ###");
						System.out.println("Resource Name: " + requestName);
						System.out.println("Resource Location: " + location);
						System.out.println("Resource CPU units: " + cpu+"mHZ");
						System.out.println("Resource Memory units: " + memory+"MB");
						System.out.println("Resource Storage units: " + storage+"GB");
						System.out.println("Full Allocation Flag: "+ resourceList.get(j).isFullAllocation());
						System.out.println("Partial Allocation Flag: "+ resourceList.get(j).isPartialAllocation());
						System.out.println("**********************************************************");
					}
					System.out.println("#####################################################################################################");
					for (int j = 0; j < requestList.size(); j++) {
						int requestID = requestList.get(j).getRequestId();
						int requestLocationID = requestList.get(j).getLocationId();
						int cpu = requestList.get(j).getCpu_units();
						int memory = requestList.get(j).getMemory();
						int storage = requestList.get(j).getStorage();
						System.out.println("**********************************************************");
						System.out.println("Request ID: " + requestID);
						System.out.println("Location ID: " + requestLocationID);
						System.out.println("Request CPU units: " + cpu+"mHZ");
						System.out.println("Request Memory units: " + memory+"MB");
						System.out.println("Request Storage units: " + storage+"GB");
						System.out.println("Request Is Allocated? "+ requestList.get(j).isAllocated());
						System.out.println("**********************************************************");
					}

					for (int i = 0; i < count; i++) {
						
						int Request_CPU_units = 0;
						int Request_Memory = 0;
						int Request_Storage = 0;
						int Request_Location = 0;
						int requestServed = 0;
						
						Request_CPU_units = requestList.get(i).getCpu_units();
						Request_Memory = requestList.get(i).getMemory();
						Request_Storage = requestList.get(i).getStorage();
						Request_Location = requestList.get(i).getLocationId();


						while ((requestList.get(i).isAllocated() == false)&& requestServed == 0) {
							for (int j = 0; j < count / 2; j++) {
								int Resource_CPU_units = resourceList.get(j).getCpu_units();
								int Resoure_Memory = resourceList.get(j).getMemory();
								int Resource_Storage = resourceList.get(j).getStorage();
								int Resource_Location = resourceList.get(j).getLocationId();

								if ((resourceList.get(j).isFullAllocation() == false) &&(Request_Location == Resource_Location)){
									if ((Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units)) > 0) {
										if (((Math.abs(Resoure_Memory)) - Math.abs(Request_Memory)) > 0) {

											if ((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) > 0) {
												Resource_CPU_units = Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units);
												Resoure_Memory = (Math.abs(Resoure_Memory)) - Math.abs(Request_Memory);
												Resource_Storage = Math.abs(Resource_Storage) - Math.abs(Request_Storage);

												// Update the resource List
												resourceList.get(j).setCpu_units(Resource_CPU_units);
												resourceList.get(j).setMemory(Resoure_Memory);
												resourceList.get(j).setStorage(Resource_Storage);

												resourceList.get(j).setFullAllocation(false);
												resourceList.get(j).setPartialAllocation(true);

												// Set the request allocated to true
												requestList.get(i).setAllocated(true);

											} else {
												if ((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) == 0) {
													Resource_CPU_units = Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units);
													Resoure_Memory = (Math.abs(Resoure_Memory))- Math.abs(Request_Memory);
													Resource_Storage = Math.abs(Resource_Storage)- Math.abs(Request_Storage);

													// Update the resource List
													resourceList.get(j).setCpu_units(Resource_CPU_units);
													resourceList.get(j).setMemory(Resoure_Memory);
													resourceList.get(j).setStorage(Resource_Storage);

													resourceList.get(j).setFullAllocation(true);
													resourceList.get(j).setPartialAllocation(false);

													// Set the request allocated to true
													requestList.get(i).setAllocated(true);
												}

											}
											if ((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) < 0) {
												System.out.println("Due to Insufficient Storage, Request cannot be processed inspite of having memory & Cpu");
												continue;
											}

										} else {
											if (((Math.abs(Resoure_Memory)) - Math.abs(Request_Memory)) == 0) {

												if ((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) > 0) {
													Resource_CPU_units = Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units);
													Resoure_Memory = (Math.abs(Resoure_Memory))- Math.abs(Request_Memory);
													Resource_Storage = Math.abs(Resource_Storage)- Math.abs(Request_Storage);

													// Update the resource List
													resourceList.get(j).setCpu_units(Resource_CPU_units);
													resourceList.get(j).setMemory(Resoure_Memory);
													resourceList.get(j).setStorage(Resource_Storage);

													resourceList.get(j).setFullAllocation(true);
													resourceList.get(j).setPartialAllocation(false);

													// Set the request allocated to true
													requestList.get(i).setAllocated(true);

												} else {
													if ((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) == 0) {
														Resource_CPU_units = Math.abs(Resource_CPU_units)- Math.abs(Request_CPU_units);
														Resoure_Memory = (Math.abs(Resoure_Memory))- Math.abs(Request_Memory);
														Resource_Storage = Math.abs(Resource_Storage)- Math.abs(Request_Storage);

														// Update the resource List
														resourceList.get(j).setCpu_units(Resource_CPU_units);
														resourceList.get(j).setMemory(Resoure_Memory);
														resourceList.get(j).setStorage(Resource_Storage);

														resourceList.get(j).setFullAllocation(true);
														resourceList.get(j).setPartialAllocation(false);

														// Set the request allocated to true
														requestList.get(i).setAllocated(true);
													}

												}
												if ((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) < 0) {
													System.out.println("Due to Insufficient Storage, Request cannot be processed inspite of having memory & Cpu");
													continue;
												}
												if (((Math.abs(Resoure_Memory)) - Math.abs(Request_Memory)) < 0) {
													System.out.println("Due to Insufficient Memory, Request cannot be processed inspite of having Cpu");
													continue;
													
												}

											}
											
										}
									} else {
										if ((Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units)) == 0) {
											if (((Math.abs(Resoure_Memory)) - Math.abs(Request_Memory)) > 0) {

												if ((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) > 0) {
													Resource_CPU_units = Math.abs(Resource_CPU_units)- Math.abs(Request_CPU_units);
													Resoure_Memory = (Math.abs(Resoure_Memory)) - Math.abs(Request_Memory);
													Resource_Storage = Math.abs(Resource_Storage)- Math.abs(Request_Storage);

													// Update the resource List
													resourceList.get(j).setCpu_units(Resource_CPU_units);
													resourceList.get(j).setMemory(Resoure_Memory);
													resourceList.get(j).setStorage(Resource_Storage);

													resourceList.get(j).setFullAllocation(true);
													resourceList.get(j).setPartialAllocation(false);

													// Set the request allocated to true
													requestList.get(i).setAllocated(true);

												} else {
													if ((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) == 0) {
														Resource_CPU_units = Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units);
														Resoure_Memory = (Math.abs(Resoure_Memory)) - Math.abs(Request_Memory);
														Resource_Storage = Math.abs(Resource_Storage) - Math.abs(Request_Storage);

														// Update the resource List
														resourceList.get(j).setCpu_units(Resource_CPU_units);
														resourceList.get(j).setMemory(Resoure_Memory);
														resourceList.get(j).setStorage(Resource_Storage);

														resourceList.get(j).setFullAllocation(true);
														resourceList.get(j).setPartialAllocation(false);

														// Set the request allocated to true
														requestList.get(i).setAllocated(true);
													}

												}
												if ((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) < 0) {
													System.out.println("Due to Insufficient Storage, Request cannot be processed inspite of having memory & Cpu");
													continue;
												}

											} else {
												if (((Math.abs(Resoure_Memory)) - Math.abs(Request_Memory)) == 0) {

													if ((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) > 0) {
														Resource_CPU_units = Math.abs(Resource_CPU_units)- Math.abs(Request_CPU_units);
														Resoure_Memory = (Math.abs(Resoure_Memory))- Math.abs(Request_Memory);
														Resource_Storage = Math.abs(Resource_Storage)- Math.abs(Request_Storage);

														// Update the resource List
														resourceList.get(j).setCpu_units(Resource_CPU_units);
														resourceList.get(j).setMemory(Resoure_Memory);
														resourceList.get(j).setStorage(Resource_Storage);

														resourceList.get(j).setFullAllocation(true);
														resourceList.get(j).setPartialAllocation(false);

														// Set the request allocated to true
														requestList.get(i).setAllocated(true);

													} else {
														if ((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) == 0) {
															Resource_CPU_units = Math.abs(Resource_CPU_units)- Math.abs(Request_CPU_units);
															Resoure_Memory = (Math.abs(Resoure_Memory))- Math.abs(Request_Memory);
															Resource_Storage = Math.abs(Resource_Storage)- Math.abs(Request_Storage);

															// Update the resource List
															resourceList.get(j).setCpu_units(Resource_CPU_units);
															resourceList.get(j).setMemory(Resoure_Memory);
															resourceList.get(j).setStorage(Resource_Storage);

															resourceList.get(j).setFullAllocation(true);
															resourceList.get(j).setPartialAllocation(false);

															// Set the request allocated to true
															requestList.get(i).setAllocated(true);
														}

													}
													if ((Math.abs(Resource_Storage) - Math.abs(Request_Storage)) < 0) {
														System.out.println("Due to Insufficient Storage, Request cannot be processed inspite of having memory & Cpu");
														continue;
													}

												}
												if (((Math.abs(Resoure_Memory)) - Math.abs(Request_Memory)) < 0) {
													System.out.println("Due to Insufficient Memory, Request cannot be processed inspite of having Cpu");
													continue;
													
												}
											}
										}
									}

									if ((Math.abs(Resource_CPU_units) - Math.abs(Request_CPU_units)) < 0) {
										System.out.println("Due to Insufficient CPU, Request cannot be processed.");
										continue;
									}

								} else {
									System.out.println("Resource Not available in the Location");
								}
							}
							if (requestList.get(i).isAllocated() == true) {
								System.out.println("******************************************************************");
								System.out.println("Request ID: "+ requestList.get(i).getRequestId()+ " is Allocated");
								System.out.println("******************************************************************");
							} else {
								System.out.println("#####################################################################");
								System.out.println("Request ID: "+ requestList.get(i).getRequestId()+ " is not Allocated");
								System.out.println("#####################################################################");
								requestServed = requestServed + 1;
							}
						}
					}

					for (int j = 0; j < resourceList.size(); j++) {
						String requestName = resourceList.get(j).getResourceName();
						int cpu = resourceList.get(j).getCpu_units();
						int memory = resourceList.get(j).getMemory();
						int storage = resourceList.get(j).getStorage();
						int location = resourceList.get(j).getLocationId();
						System.out.println("**********************************************************");
						System.out.println("### Resources After Allocation ###");
						System.out.println("Resource Name: " + requestName);
						System.out.println("Location ID: " + location);
						System.out.println("Resource CPU units: " + cpu+"mHz");
						System.out.println("Resource Memory units: " + memory+"MB");
						System.out.println("Resource Storage units: " + storage +"GB");
						System.out.println("Full Allocation Flag: "+ resourceList.get(j).isFullAllocation());
						System.out.println("Partial Allocation Flag: "+ resourceList.get(j).isPartialAllocation());
						System.out.println("**********************************************************");
					}

					resourceNames = resourceStorage.updateResourcesInHashMap(resourceList);
					
					Map<String, Object> resourceTemplate = new HashMap<String, Object>();
					resourceTemplate.put("Resource Name: ", resourceNames);
					
					/*return new ModelAndView(resourceTemplate, "resource.ftl");
				}, new FreeMarkerEngine());*/

					return "**********Following Resources Allocated*********"+ resourceNames;

				});

		// Billing is done based on ant colony algorithm
		get("/antColonyAllocation/billing",(request, response) -> {

					final double costOfCpuUnints = 0.30;
					final double costOfMemoryUnits = 0.50;
					final double costOfStorageUnits = 0.70;

					double totalCostOfCpuUnits = 0.0;
					double totalCostOfMemoryUnits = 0.0;
					double totalCostOfStorageUnits = 0.0;
					double totalBill = 0.0;
					double sumOfTotalBills = 0.0;
					
					Map<String, Object> billing = new HashMap<>();

					System.out.println("**********************************************************");
					System.out.println("The cost of CPU units: $"+ costOfCpuUnints + "/units");
					System.out.println("The cost of Memory units: $"+ costOfMemoryUnits + "/units");
					System.out.println("The cost of Storage units: $"+ costOfStorageUnits + "/units");
					System.out.println("**********************************************************");

					ArrayList<ResourceRequest> allocatedResources = new ArrayList<ResourceRequest>();
					allocatedResources = requestResourceStorage.getAllocatedRequestFromHashMap();

					if (allocatedResources.isEmpty() == false) {

						for (int i = 0; i < allocatedResources.size(); i++) {
							if ((allocatedResources.get(i).isAllocated() == true)) {
								totalCostOfCpuUnits = Math.abs(allocatedResources.get(i).getCpu_units())* costOfCpuUnints;
								totalCostOfMemoryUnits = Math.abs(allocatedResources.get(i).getMemory())* costOfMemoryUnits;
								totalCostOfStorageUnits = Math.abs(allocatedResources.get(i).getStorage())* costOfStorageUnits;
								totalBill = totalCostOfCpuUnits+ totalCostOfMemoryUnits+ totalCostOfStorageUnits;

								System.out.println("*****************************************************************");
								System.out.println("Allocated Request ID: "+ allocatedResources.get(i).getRequestId());
								System.out.println("Cost of allocated CPU Units: $"+ totalCostOfCpuUnits);
								System.out.println("Cost of allocated Memory Units: $"+ totalCostOfMemoryUnits);
								System.out.println("Cost of allocated Storage Units: $"+ totalCostOfStorageUnits);
								System.out.println("Total Bill for the allocated Resources: $"+ totalBill);
								System.out.println("*****************************************************************");

								billing.put("Allocated Request ID -- ",allocatedResources.get(i).getRequestId());
								billing.put("Cost of allocated CPU Units -- ",totalCostOfCpuUnits);
								billing.put("Cost of allocated Memory Units -- ",totalCostOfMemoryUnits);
								billing.put("Cost of allocated Storage Units -- ",totalCostOfStorageUnits);
								billing.put("Total Bill for the allocated Resources --",totalBill);

							}
							sumOfTotalBills = totalBill + sumOfTotalBills;
						}
					} else {
						double value = 0.0;
						billing.put("Allocated Resource Name -- ", value);
						billing.put("Cost of allocated CPU Units -- ", value);
						billing.put("Cost of allocated Memory Units -- ", value);
						billing.put("Cost of allocated Storage Units -- ",value);
						billing.put("Total Bill for the allocated Resources --",value);

					}
					Map<String, Object> billingTemplate = new HashMap<String, Object>();
					billingTemplate.put("billing", billing);

					return new ModelAndView(billingTemplate, "billing.ftl");
				}, new FreeMarkerEngine());

		// Location Based allocation done using ant colony algorithm
		get("/antColonyAllocation/location", (request, response) -> {

			HashMap<String, List<String>> locationBasedAllocatedResources = new HashMap<String, List<String>>();
			
			locationBasedAllocatedResources=locationBased.getLocationBasedAllocatedResources();
			Iterator<Map.Entry<String, List<String>>> locationIterator = locationBasedAllocatedResources.entrySet().iterator() ;
			while(locationIterator.hasNext()){
				Map.Entry<String, List<String>> locationEntry = locationIterator.next();
				String LocationName = locationEntry.getKey();
				List<String> ResourceName = (ArrayList<String>) locationEntry.getValue();
				System.out.println("**********************************************************************");
				System.out.println("Location Name: "+LocationName);
				for(int i = 0; i<ResourceName.size(); i++)
				{
					System.out.println("Resource Name: "+ ResourceName.get(i));
				}
				System.out.println("**********************************************************************");
				
			}
			
			/*Map<String, Object> locationDetails = new HashMap<>();
			for(int i =0; i< location.size(); i++){
				
				ArrayList <Integer> AllocatedLocationID = (ArrayList<Integer>)location.keySet();
				String resourceName = location.get(AllocatedLocationID.get(i));
				String allocatedLocationID = AllocatedLocationID.get(i).toString();
				
				locationDetails.put("Location Id: ", allocatedLocationID);
				locationDetails.put("Resource Name: ", resourceName);
			}*/
			
			
			/*Map<String, Object> locationTemplate = new HashMap<String, Object>();
			locationTemplate.put("message", "Check the console for output");
			
			return new ModelAndView(locationTemplate, "locationHeader.ftl");
		}, new FreeMarkerEngine());*/
			return "**************Allocated Resources With Location Name************** "+locationBasedAllocatedResources;
		});
		
		get("/antColonyAllocation/cloudwatch", (request, response) -> {
					
			Map<String, Object> cloudwatch = new HashMap<String, Object>();
			cloudwatch.put("message", "Recent Metrices");
			
			return new ModelAndView(cloudwatch, "cloudwatch.ftl");
			//return new ModelAndView(cloudwatch, "test.ftl");
		}, new FreeMarkerEngine());

	}

}