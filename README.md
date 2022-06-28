# ePTM_v2
The Enhanced Particle Tracking Model Version 2.0 (ePTM v2) is a computational model that simulates the oceanward migration of fish that spawn in rivers and mature in the Ocean. It is a synthetic fish telemetry system that allows scientists, practioners and natural resources managers understand how aquatic organisms such as fish use their environment.

![Alt Text](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/animOutputCropped_1.gif)

Information on installing the model, using it, and its structure can be found in the model Wiki pages here: [Link](https://github.com/cvclcm/ePTM_v2/wiki).

If you have questions regarding the model, please contact *vamsi.sridharan@noaa.gov*.


# Table of contents
1. Introduction
2. Use case
3. Background
4. Computing requirements
6. Project information

# Introduction
The model can be described in a variety of ways to varying degrees of specificity:

This model is a decision support tool, because it is being, and can be used to produce the scientific insights necessary to make management and policy decisions on water supply in a multipurpose surface water system under a regime of uncertainty due to factors such as climate change, sea-level rise, ageing infrastructure and changing land use and land cover.

It is a type of agent-based model, in which simulated fish interact with their local aquatic environment according to a set of behavior rules or "agents." 

It is also an individual-based model in that the migratory life histories of individual simulated fish are tracked over a large spatial region. Although, we must note that, in its current form, the model does not simulate the interactions within schools of fishes, or between predators and their prey in an explicit way.

This is is a process-based, or mechanistic model which uses physical and biological constraints based on observations of real fish to direct the movement of simulated fish. Specifically, this is a particle tracking model or stochastic random walk model that simulates the self-correlated movements of simulated fish subject to their local environment, which in this case is the flow of water and the time of day. 

Finally, this is a fully data-driven model. Animal migration through complex environments is a fundamentally multi-scalar process, and in specifying the movement rules in this model, we have adopted the best available acoustic telemetry data on moving fish. 

**High-resolution acoustically tagged Chinook salmon tracks at a channel junction and their exposure to tagged predators in the Sacramento River allow us to study mesoscale behaviors and specify behavior rules to the simulated fish**

![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/Tracks2011.jpg) 

**Migration statistics of acoustically tagged Chinook salmon moving from the Sacramento River towards the ocean reveal macroscopic patterns that are used to calibrate the behavior parameters in the model** 

![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/MigrationRateVsReach.jpg)

This model has been developed through a thorough review of the scientific literature on salmonid movement, stakeholder engagement, and original scientific research.
Fundmental discoveries about the mesoscale movement behaviors of fish were reported by us in a recent paper by Olivetti et al. (2021) here: [Link](https://besjournals.onlinelibrary.wiley.com/doi/full/10.1111/2041-210X.13604?casa_token=R24dUwXx0FcAAAAA%3AUYvokc4iyCMO8VpBF6dAU1C306BvSg4r60KTjsV27wgm0W_EKtuH34H8F6m2rLX9-JD7LwvseX23zHU).
A macroscopic representation of salmonid movements through estuaries was developed and reported by us in a recent paper by Sridharan et al. (2019) here: [Link](https://agupubs.onlinelibrary.wiley.com/doi/full/10.1029/2019WR025429?casa_token=8r5Cth5KgH4AAAAA%3Ac67189ZeR9wPmDHx6iglofh9-e719x0znYf-Svraj5li_6-EaScwiWG_0wH9kZhUKxO3HX8V31_CEfo).

## Why do you need ePTM v2?
If you are a scientist who wants to understand how migratory aquatic animals navigate complex surface waters, or you are a water manager or conservation policy maker who needs to know what impact a new water project or policy will have on local wildlife, you need a nimble, reliable model to help you make sense of what's going on. Acoustic telemetry programs are expensive to run, and only provide patchy data. What happens in those regions where there is limited data coverage? But a statistical model that links macroscopic system variables such as flow with survival will surely do, you might think. What happens when the statistical relationships change due to uncertainty from climate change and other factors? A mechanistic model like the ePTM can predict what is likley to happen to fish in novel environmental conditions. Below are model validation results (comparison of model performance with data held out during calibration).

**Out of sample validation of the flow versus survival relationship in for Winter-run juvenile Chinook salmon in the Sacramento San Joaquin Delta. A low bias (<0.1), significant overlap in the 95% confidence intervals in the acoustic telemetry data and model results and functionally similar relationships recovered by the model indicates its value**

![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/FlowvsSurvival.jpg)

**Out of sample validation of the probability of entrainment into different migratory routes during flood vs ebb tides indicates that the model is able to predict migratory routes well. In 2014, a floating fish guidance structure was placed at the head of Georgiana Slough which we only approximately represented in the model**

![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/RoutingProbabilties.jpg)

# Use case
Currently ePTM v2 has been calibrated for juvenile Chinook salmon migrating through the Sacramento-San Joaquin Delta in Central California towards the San Francisco Bay and the Pacific Ocean. It requires numerical simulations of the river currents and tidal flows which are provided by the California Department of Water Resources (DWR) Delta Simulation Model (DSM2) v.8.1.2, which is hosted by DWR here: [Link](https://github.com/CADWRDeltaModeling/dsm2).

**ePTM v2 model domain for application to Sacramento River Winter run Chinook salmon smolt migration with the Sacramento River highlighted**

![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/DeltaMapwSacRiver.jpg)

In the context of the California Central Valley, this model is used to provide monthly survival estimates for endangered juvenile Sacramento River Winter-run Chinook salmon rearing and migrating through the Sacramento-San Joaquin Delta. These estimates are then used within a larger stage-structured population dynamics model, the Central Valley Life Cycle Model: [Link](https://oceanview.pfeg.noaa.gov/wrlcm/intro).

At the moment, the model has not been configured to run with other hydrodynamic engines. So, if you want to develop a model using ePTM v2 and DSM2 for your system, you will have to create a model grid, calibrate and validate the DSM2 model for predicting flows using this grid, and generate hydrodynamic outputs, which can then be used with ePTM v2. 

![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/SacRiverWinterRunChinookSmolt_SiskoJakeUSFWS.jpg)

**Sacramento River Winter-run Chinook salmon smolt (Photo credit: Jake Sisko, US Fish and Wildlife service)**

In a similar vein, at the moment, the model has behavior rules appropriate for simulating juvenile salmonid migration, and the model has been calibrated and validated for Chinook salmon of the Sacramento and San Joaquin River systems in California using data published by Perry (2010; 2012) [Link](https://github.com/cvclcm/ePTM_v2/tree/main/documentation). A detailed description of the model calibration and validation can be found here: [Link](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/Sridharan_ePTMv2_CalibrationValidation_Dec1_2021.pptx). All the data used to build, calibrate and validate the model has been archived at [Link](https://github.com/cvclcm/ePTM_v2/tree/main/Data).

You can make modifications to the source code to change behavior rules, and recalibrate the model for other species of fish in other systems. Please see the Wiki pages here [Link](https://github.com/cvclcm/ePTM_v2/wiki) for instructions on how to do this. 

# Background
ePTM v2 adds behavior classes to DWR's Particle Tracking Model (PTM), which simulates neutrally buoyant, passive tracers. Both the PTM and the ePTM v2 require a hydrodynamic engine to simulate water velocities and Depths. This is achieved by the DSM2 model, a one-dimensional shallow water equation solver. A detailed peer review of the model can be found in the paper by Sridharan et al. (2018) [Link](https://escholarship.org/uc/item/0vm955tw). The ePTM v2 also includes significant numerical improvements to the underlying random walk algorithms in the PTM, most of which are detailed in the paper by Sridharan et al. (2017) [Link](https://ascelibrary.org/doi/full/10.1061/(ASCE)HY.1943-7900.0001399?casa_token=yMf5O160xyoAAAAA:v891cN9CzxrTTSQBoi3FTBvZsfMKstbIU1Et8QPf5Dh6dHIJsE-wh8eotqCa2S-8X-MV4hgnXA).

The ePTM v2 simulates three aspects of aquatic animal movement: migration by swimming, route selection through complex topologies such as river branches and channel junctions in the presence of flow reversals due to tides and water operations, and mortality by predation. While the details of these mechanisms are too complex to discuss here, a brief description of the model structure is available here: [Link](https://oceanview.pfeg.noaa.gov/wrlcm/documents/documentation/ePTM_Study_Plan_FINAL_02_12_2021_v2.pdf). This document also contains information on how the model continues to be, and can be used in a variety of water management applications. We have made a video describing the biological behavior rules in the model, which can be found here: [Link](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/Sridharan_04_09_21_BDSC_ePTMV2Description.mp4). 

## Model development process
This is a scientific model that has been developed with the following core principles:
1. Be self-consistent across multiple scales of motion and include realistic physical and biological processes
2. Be nimble and scalable enough to be deployed for multi-decadal population dynamics simulations and be versatile enough to be used either as a standalone or as a component of a larger modeling ecosystem while yet retaining state-of-the-art numerical sophistication and realism
3. Be borne out of an inclusive and transparent process through multi-entity collaboration which utilizes high-quality scientific data, a multidisciplinary development team and active stakeholder engagement with domain experts and decision makers in the California Central Valley.

The ePTM v1.0 was developed in collboration between the National Marine Fisheries Service, the California Department of Fish and Wildlife, and the United States Geological Survey. This model extended the DSM2-PTM model to include a suite of possible fish behaviors. With subsequent generous support from the Bureau of Reclamation to integrate the model into the Chinook Salmon Life Cycle Model, and to participate in stakeholder engagement to aid in brainstorming the model, and from the California Department of Fish and Wildlife to combine multiscalar data sources to develop a more fundamental understanding of migratory movements of salmonids, version 2.0 was developed with improved hydrodynamic representations of the flow and more realistic behavior models. 

Throguhout the model development process, we have engaged with the local, regional and federal stakeholder community. A history of our engagements and stakeholder supplied behavioral hypothesis which are in various stages of evaluation with the model can be found here: [Link](https://oceanview.pfeg.noaa.gov/wrlcm/resources) under the *Workshops* and *Stakeholder shared documents* tabs, respectively.

# Computing requirements
The ePTM v2 is developed in Java and currently runs only on Windows PCs. It can be run in one-shot or batch mode. The following hardware and software dependencies are required to run the model:

1. A powerful laptop or desktop to run multiple months or years of simulations, and a high performance computer or cluster or cloud computing to run multi-decadal simulations 
2. 32-bit Java runtime environment 8 to compile and run the model [![Image](https://img.shields.io/badge/java-jre%201.8-blue)](https://www.java.com/download/ie_manual.jsp)
3. An IDE environment such as Eclipse to work with the model source code. In the installation guide below, instructions will be available on using Eclipse: [Link](https://www.eclipse.org/downloads/packages/release/juno/sr1/eclipse-ide-java-ee-developers). **Note that you will need an IDE that supports 32-bit Java 8**, which you can find from here [![Image](https://img.shields.io/badge/Eclipse-4.16-red)](https://archive.eclipse.org/eclipse/downloads/).
4. DSM2 v.8.1.2 which is the hydrodynamic engine of the model [![Image](https://img.shields.io/badge/DSM2-v.8.1.2-orange)](https://water.ca.gov/Library/Modeling-and-Analysis/Bay-Delta-Region-models-and-tools/Delta-Simulation-Model-II) 
5. HDF5 suite of tools for visualizing and manipulating hydrodynamic model results: [Link](https://www.hdfgroup.org/solutions/hdf5/)
6. A good text editor such as Notepad++: [Link](https://notepad-plus-plus.org/downloads/)
7. Python 3.x to build scripts for running the model and processing outputs [![Image](https://img.shields.io/badge/python-3.7%2B-green)](https://www.python.org/downloads/) 
8. R 4.x or Matlab 2018b or above for analyzing model results [![Image](https://img.shields.io/badge/R-4.0%2B-yellow)](https://www.r-project.org/)
9. HEC-DssVue to visualize hydrodynamic time series from DSM2: [Link](https://www.hec.usace.army.mil/software/hec-dssvue/)
10. Google Earth Pro to visualize the DSM2 grid (this will be invaluable when setting up the model for cutom use in the California Central Valley): [Link](https://www.google.com/earth/download/gep/agree.html?hl=en-GB)

# Project information
Here, we will update the latest news, information and status of the project.

## News

![Image](https://img.shields.io/static/v1?label=01/12&message=2022&labelColor=lightgrey&color=brightgreen&style=plastic&logo=githubactions)
The final report to the California Department of Fish and Wildlife on the Proposition 1 project that partially supported the development fo the ePTM model is now available here: [Link](https://github.com/cvclcm/ePTM_v2/blob/main/documentation/Raimondi_et_al_2021_CDFW_FinalReport.pdf). Please cite as:

Raimondi, P., Hein, A., Sridharan, V., Danner, E., and Lindley, S. 2021. A next-generation model of juvenile salmon migration through the Sacramento-San Joaquin Delta. Report submitted by the University of California Santa Cruz to the California Department of Fish and Wildlife Under Proposition 1 Scientific Award No. P1896007. Sacta Cruz, CA. 55p. Available at: https://github.com/cvclcm/ePTM_v2/blob/main/documentation/Raimondi_et_al_2021_CDFW_FinalReport.pdf.  

![Image](https://img.shields.io/static/v1?label=12/01&message=2021&labelColor=lightgrey&color=brightgreen&style=plastic&logo=githubactions)
The ePTM v2 is currently being used in the evaluation of alternative water management operations under climate change and seal-level rise for the Delta Conveyance Project in California.

![Image](https://img.shields.io/static/v1?label=01/06&message=2022&labelColor=lightgrey&color=brightgreen&style=plastic&logo=githubactions)
The ePTM v2 was featured in an article on Maven's notebook here:

https://mavensnotebook.com/2022/01/06/bay-delta-science-conference-applying-the-winter-run-life-cycle-model-to-pressing-hydromanagement-questions-in-the-central-valley/

![Image](https://img.shields.io/static/v1?label=06/28&message=2022&labelColor=lightgrey&color=brightgreen&style=plastic&logo=githubactions)
Please note correction in ePTMCreatInputs.R usage in the Wiki. For specifying checkpoints, please use DSM2 *External* node numbers. This had been reported previously incorrectly as *Internal* node numbers.

## Future improvements
There are a few areas in which the model can be improved:

1. Since we know that fish do not behave like passive particles that simply go with the flow (Sridharan et al. 2017; Hance et al. 2020; Gross et al. 2021), the lateral and vertical random walk movements should be updated to draw from observed distributions conditional on local flows rather than simply allowing simulated fish to be kicked by turbulent eddies.
2. Additional efficiencies may be gained by studying where in a complex surface water system movements are predominantly 2d and 3d in nature, and where 1d along-stream movement simulation may suffice. 
3. Using water velocities measured within the actual physical domain of the model under a variety of flow conditions rather than experimental flow profiles obtained in the laboratory flume.
4. Updating the code to use 64-bit Java.
5. Updating the code to run on Linux and Mac as well.
6. Updating how the code interacts with DSM2 so that it can be run independently of the underlying hydrodynamic engine.

Would you like to contribute to these improvements? If so, please fork the code to your own repo, and send us a pull request once you have a stable build

## Core team
The core model development team comprises of the following entities:

University of California, Santa Cruz: Institute of Marine Sciences

Southwest Fisheries Science Center, National Marine Fisheries Service, National Oceanic and Atmospheric Administration

Western Fisheries Research Center, United States Geological Survey

QEDA Consulting, LLC.

## Acknowledgements
The core modeling team acknowledges the California Department of Water Resources for their significant contributions to the project.

We also thank the Fisheries Cooperative Program at the University od California, Santa Cruz, for providing technical and program support for the project.

We are grateful to the United States Geological Survey, the California Department of Fish and Wildlife, the United States Fish and Wildlife Service and the University of Washington, Seattle, for providing the data used to build, calibrate and the validate the model.

We would also like to acknowledge the domain expertise of various local, regional and federal agency, and academic and industry stakeholders which we incorporated during early model development. 

## Funding
Principle support for the scientific fundamentals of the development of ePTM v2 has been provided by a California Proposition 1 scientific research grant awarded by the California Deparment of Fish and Wildlife.

Principle support for the integration of the model for decision support in the California Central Valley, and for stakeholder engagement has been provided by the Bureau of Reclamation through a cooperative agreement with the University of California, Santa Cruz.

**Our funding partners**

![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/CDFWLogo.png) ![Image](https://github.com/cvclcm/ePTM_v2/blob/main/SupportingMaterial/BORLogo.png)
