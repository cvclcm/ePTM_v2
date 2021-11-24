# ePTM_v2
The Enhanced Particle tracking Model Version 2.0 

The Enhanced Particle Tracking Model Version 2.0 (ePTM v2) is a computational model that simulates the oceanward migration of fish that spawn in rivers and mature in the Ocean. The model can be described in a variety of ways to varying degrees of specificity:

This model is a decision support tool, because it is being, and can be used to produce the scientific insights necessary to make management and policy decisions on water supply in a multipurpose surface water system under a regime of uncertainty due to factors such as climate change, sea-level rise, ageing infrastructure and changing land use and land cover.

It is a type of agent-based model, in which simulated fish interact with their local aquatic environment according to a set of behavior rules or "agents." 

It is also an individual-based model in that the migratory life histories of individual simulated fish are tracked over a large spatial region. Although, we must note that, in its current form, the model does not simulate the interactions within schools of fishes, or between predators and their prey in an explicit way.

This is is a process-based, or mechanistic model in that, rather than specifying behavior rules through statistical cause-and-effect relationships, the model uses physical and biological constraints based on observations of real fish to direct the movement of simulated fish. Specifically, this is a particle tracking model or stochastic random walk model that simulates the self-correlated movements of simulated fish subject to their local environment, which in this case is the flow of water and the time of day. 

Finally, this is a fully data-driven model. Animal migration through complex environments is a fundamentally multi-scalar process, and in specifying the movement rules in this model, we have adopted the best available acoustic telemetry data on moving fish.

## Application
Currently ePTM v2 has been calibrated for juvenile Chinook salmon migrating through the Sacramento-San Joaquin Delta in Central California towards the San Francisco Bay and the Pacific Ocean. It requires numerical simulations of the river currents and tidal flows which are provided by the California Department of Water Resources (DWR) Delta Simulation Model v.8.1.2, which is hosted by DWR here: [Link](https://github.com/CADWRDeltaModeling/dsm2)
