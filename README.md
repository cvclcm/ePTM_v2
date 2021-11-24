# ePTM_v2
The Enhanced Particle tracking Model Version 2.0 

The Enhanced Particle Tracking Model Version 2.0 (ePTM v2) is a computational model that simulates the oceanward migration of fish that spawn in rivers and mature in the Ocean. It is a synthetic fish telemetry system that allows scientists, practioners and natural resources managers understand how aquatic organisms such as fish use their environment.

![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/ePTMv2Logo.jpg)

# Table of contents
1. Introduction
2. Use case
3. Background
4. Computing requirements
5. Installation
6. Project information

# Introduction
The model can be described in a variety of ways to varying degrees of specificity:

This model is a decision support tool, because it is being, and can be used to produce the scientific insights necessary to make management and policy decisions on water supply in a multipurpose surface water system under a regime of uncertainty due to factors such as climate change, sea-level rise, ageing infrastructure and changing land use and land cover.

It is a type of agent-based model, in which simulated fish interact with their local aquatic environment according to a set of behavior rules or "agents." 

It is also an individual-based model in that the migratory life histories of individual simulated fish are tracked over a large spatial region. Although, we must note that, in its current form, the model does not simulate the interactions within schools of fishes, or between predators and their prey in an explicit way.

This is is a process-based, or mechanistic model in that, rather than specifying behavior rules through statistical cause-and-effect relationships, the model uses physical and biological constraints based on observations of real fish to direct the movement of simulated fish. Specifically, this is a particle tracking model or stochastic random walk model that simulates the self-correlated movements of simulated fish subject to their local environment, which in this case is the flow of water and the time of day. 

Finally, this is a fully data-driven model. Animal migration through complex environments is a fundamentally multi-scalar process, and in specifying the movement rules in this model, we have adopted the best available acoustic telemetry data on moving fish. 

**High-resolution acoustically tagged Chinook salmon tracks at a channel junction and their exposure to tagged predators in the Sacramento River allow us to study mesoscale behaviors and specify behavior rules to the simulated fish**

![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/Tracks2011.jpg) 

**Migration statistics of acoustically tagged Chinook salmon moving from the Sacramento River towards the ocean reveal macroscopic patterns that are used to calibrate the behavior parameters in the model** 

![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/MigrationRateVsReach.jpg)

This model has been developed through a thorough review of the scientific literature on salmonid movement, stakeholder engagement, and original scientific research.
Fundmental discoveries about the mesoscale movement behaviors of fish were reported by us in a recent paper by Olivetti et al. (2021) here: [Link](https://besjournals.onlinelibrary.wiley.com/doi/full/10.1111/2041-210X.13604?casa_token=R24dUwXx0FcAAAAA%3AUYvokc4iyCMO8VpBF6dAU1C306BvSg4r60KTjsV27wgm0W_EKtuH34H8F6m2rLX9-JD7LwvseX23zHU).
A macroscopic representation of salmonid movements through estuaries was developed and reported by us in a recent paper by Sridharan et al. (2019) here: [Link](https://agupubs.onlinelibrary.wiley.com/doi/full/10.1029/2019WR025429?casa_token=8r5Cth5KgH4AAAAA%3Ac67189ZeR9wPmDHx6iglofh9-e719x0znYf-Svraj5li_6-EaScwiWG_0wH9kZhUKxO3HX8V31_CEfo).

# Use case
Currently ePTM v2 has been calibrated for juvenile Chinook salmon migrating through the Sacramento-San Joaquin Delta in Central California towards the San Francisco Bay and the Pacific Ocean. It requires numerical simulations of the river currents and tidal flows which are provided by the California Department of Water Resources (DWR) Delta Simulation Model (DSM2) v.8.1.2, which is hosted by DWR here: [Link](https://github.com/CADWRDeltaModeling/dsm2).

**ePTM v2 model domain for application to Sacramento River Winter run Chinook salmon smolt migration with the Sacramento River highlighted**

![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/DeltaMapwSacRiver.jpg)

In the context of the California Central Valley, this model is used to provide monthly survival estimates for endangered juvenile Sacramento River Winter-run Chinook salmon rearing and migrating through the Sacramento-San Joaquin Delta. These estimates are then used within a larger stage-structured population dynamics model, the Central Valley Life Cycle Model: [Link](https://oceanview.pfeg.noaa.gov/wrlcm/intro).

At the moment, the model has not been configured to run with other hydrodynamic engines. So, if you want to develop a model using ePTM v2 and DSM2 for your system, you will have to create a model grid, calibrate and validate the DSM2 model for predicting flows using this grid, and generate hydrodynamic outputs, which can then be used with ePTM v2. 

# Background
ePTM v2 adds behavior classes to DWR's Particle Tracking Model (PTM), which simulates neutrally buoyant, passive tracers. Both the PTM and the ePTM v2 require a hydrodynamic engine to simulate water velocities and Depths. This is achieved by the DSM2 model, a one-dimensional shallow water equation solver. A detailed peer review of the model can be found in the paper by Sridharan et al. (2018) [Link](https://escholarship.org/uc/item/0vm955tw)). The ePTM v2 also includes significant numerical improvements to the underlying random walk algorithms in the PTM, most of which are detailed in the paper by Sridharan et al. (2017) [Link](https://ascelibrary.org/doi/full/10.1061/(ASCE)HY.1943-7900.0001399?casa_token=yMf5O160xyoAAAAA:v891cN9CzxrTTSQBoi3FTBvZsfMKstbIU1Et8QPf5Dh6dHIJsE-wh8eotqCa2S-8X-MV4hgnXA).

The ePTM v2 simulates three aspects of aquatic animal movement: migration by swimming, route selection through complex topologies such as river branches and channel junctions in the presence of flow reversals due to tides and water operations, and mortality by predation. While the details of these mechanisms are too complex to discuss here, a brief description of the model structure is available here: [Link](https://oceanview.pfeg.noaa.gov/wrlcm/documents/documentation/ePTM_Study_Plan_FINAL_02_12_2021_v2.pdf). This document also contains information on how the model continues to be, and can be used in a variety of water management applications. We have made a video describing the biological behavior rules in the model, which can be found here: [Link](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/Sridharan_04_09_21_BDSC_ePTMV2Description.mp4). 

## Model development process
This is a scientific model that has been developed with the following core principles:
1. Be self-consistent across multiple scales of motion and include realistic physical and biological processes
2. Be nimble and scalable enough to be deployed for multi-decadal population dynamics simulations and be versatile enough to be used either as a standalone or as a component of a larger modeling ecosystem while yet retaining state-of-the-art numerical sophistication and realism
3. Be borne out of an inclusive and transparent process through multi-entity collaboration which utilizes hiqgh-quality scientific data, a multidisciplinary development team and active stakeholder engagement with domain experts and decision makers in the California Central Valley.

The ePTM v1.0 was developed in collboration between the National Marine Fisheries Service, the California Department of Fish and Wildlife, and the United States Bureau of Reclamation. This model extended the DSM2-PTM model to include a suite of possible fish behaviors. With subsequent generous support from the Bureau of Reclamation to integrate the model into the Chinook Salmon Life Cycle Model, and to participate in stakeholder engagement to aid in brainstorming the model, and from the California Department of Fish and Wildlife to combine multiscalar data sources to develop a more fundamental understanding of migratory movements of salmonids, version 2.0 was developed with improved hydrodynamic representations of the flow and more realistic behavior models. 

Throguhout the model development process, we have engaged with the local, regional and federal stakeholder community. A history of our engagements and stakeholder supplied behavioral hypothesis which are in various stages of evaluation with the model can be found here: [Link}(https://oceanview.pfeg.noaa.gov/wrlcm/resources).

# Computing requirements
The ePTM v2 is developed in Java and currently runs only on Windows PCs. It can be run in one-shot or batch mode. The following hardware and software dependencies are required to run the model:

![Your Repository's Stats](https://github-readme-stats.vercel.app/api/top-langs/?username=cvclcm&theme=blue-green)

1. A powerful laptop or desktop to run multiple months or years of simulations, and a high performance computer or cluster or cloud computing to run multi-decadal simulations 
2. 32-bit Java runtime environment 8 to compile and run the model ![Image](https://img.shields.io/badge/java-jre%201.8-blue): [Link](https://www.java.com/download/ie_manual.jsp)
3. An IDE environment such as Eclipse to work with the model source code. In the installation guide below, instructions will be available on using Eclipse: [Link](https://www.eclipse.org/downloads/packages/release/juno/sr1/eclipse-ide-java-ee-developers)
4. DSM2 v.8.1.2 which is the hydrodynamic enging of the model ![Image](https://img.shields.io/badge/DSM2-v.8.1.2-orange): [Link](https://water.ca.gov/Library/Modeling-and-Analysis/Bay-Delta-Region-models-and-tools/Delta-Simulation-Model-II) 
5. HDF5 suite of tools for visualizing and manipulating hydrodynamic model results: [Link](https://www.hdfgroup.org/solutions/hdf5/)
6. A good text editor such as Notepad++: [Link](https://notepad-plus-plus.org/downloads/)
7. Python 3.x to build scripts for running the model and processing outputs ![Image](https://img.shields.io/badge/python-3.7%2B-green): [Link](https://www.python.org/downloads/) 
8. R 4.x or Matlab 2018b or above for analyzing model results ![Image](https://img.shields.io/badge/R-4.0%2B-yellow): [Link](https://www.r-project.org/)
9. HEC-DssVue to visualize hydrodynamic time series from DSM2: [Link](https://www.hec.usace.army.mil/software/hec-dssvue/)
10. Google Earth Pro to visualize the DSM2 grid (this will be invaluable when setting up the model for cutom use in the California Central Valley): [Link](https://www.google.com/earth/download/gep/agree.html?hl=en-GB)

# Project information
Here, we will update the latest news, information and status of the project.

## News
The ePTM v2 is currently being used in the evaluation of alternative water management operations under climate change and seal-level rise for the Delta Conveyance Project in California.

## Core team
The core model development team comprises of the following entities:

University of California, Santa Cruz: Institute of Marine Sciences

Southwest Fisheries Sience Center, National Marine Fisheries Service, National Oceanic and Atmospheric Administration

United States Bureeau of Reclamation

Qeda Consulting, LLC.

![GitHub Contributors Image](https://contrib.rocks/image?repo=cvclcm/ePTM_v2)

Made with [contributors-img](https://contrib.rocks).

## Funding
Principle support for the scientific fundamentals of the development of ePTM v2 has been provided by a California Proposition 1 scientific research grant awarded by the California Deparment of Fish and Wildlife
Principle support for the integration of the model for decision support in the California Central Valley, and for stakeholder engagement has been provided by the Bureau of Reclamation through a cooperative agreement with the University of California, Santa Cruz.

**Our funding partners**

![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/CDFWLogo.png) ![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/BORLogo.png)
