# IHGWSO-LoadBalancer
Improved Hybrid Grey Wolf–Particle Swarm Optimization for Cloud Load Balancing

# IHGWSO: Improved Hybrid Grey Wolf–Particle Swarm Optimization for Cloud Load Balancing

## Overview

This repository contains the source code and experimental implementation of the **Improved Hybrid Grey Wolf–Particle Swarm Optimization (IHGWSO)** algorithm, developed to improve load balancing and task scheduling in cloud computing environments.

IHGWSO combines the exploration capabilities of the Grey Wolf Optimizer (GWO) with the exploitation capabilities of Particle Swarm Optimization (PSO). The proposed approach incorporates adaptive parameter adjustment to improve convergence, optimize cloud resource allocation, and reduce workload imbalance.

The algorithm is evaluated using the **CloudSim simulation framework** against standard GWO, PSO, and the hybrid Genetic Algorithm–Grey Wolf Optimizer (HGA-GWO).

## Research Objectives

The primary objectives of the proposed IHGWSO algorithm are:

- Minimize average response time.
- Minimize makespan.
- Maximize throughput.
- Maximize average resource utilization.
- Minimize the degree of imbalance.
- Improve convergence behavior under varying workloads.

## Proposed IHGWSO Algorithm

The proposed algorithm integrates the following optimization mechanisms:

1. **GWO-based exploration:** Identifies promising cloudlet-to-VM mappings using alpha, beta, and delta wolves.
2. **PSO-based refinement:** Improves candidate solutions through velocity and position updates.
3. **Adaptive parameter adjustment:** Dynamically adjusts inertia weight and cognitive/social coefficients to balance exploration and exploitation.
4. **Multi-objective fitness evaluation:** Evaluates candidate solutions using weighted QoS-related fitness components.
5. **Convergence control:** Terminates optimization based on the maximum iteration count or stagnation threshold.

The final output is an optimized mapping of cloudlets to virtual machines.

## Experimental Evaluation

The proposed algorithm is evaluated through three experiments.

### Experiment 1: Multi-Scale Cloud Environments

| Parameter | Small Scale | Medium Scale | Large Scale |
|---|---|---|---|
| Datacenters | 1 | 2 | 5 |
| Hosts per datacenter | 5 | 10 | 20 |
| Virtual machines | 10 | 50 | 80 |
| Cloudlets | 30 | 100 | 500 |
| Cloudlet length (MI) | 1,000–5,000 | 5,000–10,000 | 10,000–20,000 |

### Experiment 2: Dynamic Workload Evaluation

The algorithm is evaluated under dynamically varying workloads to assess its ability to maintain efficient scheduling and resource utilization as workload demands change.

The number of cloudlets varies from 100 to 1,000.

### Experiment 3: Scalability and Stress Testing

Stress testing evaluates algorithm behavior under increasing workloads and identifies performance saturation boundaries.

The experimental results demonstrate approximately 88% average resource utilization at 10,000 cloudlets, reaching a saturation level of approximately 89.96% at 20,000 cloudlets.

These results reflect the evaluated CloudSim configurations rather than guaranteed performance in real-world cloud deployments.

## Optimization Parameters

| Parameter | Value |
|---|---|
| Population size (wolves) | 12 |
| Maximum iterations | 100 |
| Maximum inertia weight | 0.9 |
| Minimum inertia weight | 0.4 |
| Initial cognitive coefficient | 2.0 |
| Final cognitive coefficient | 0.5 |
| Initial social coefficient | 0.5 |
| Final social coefficient | 2.0 |
| Convergence tolerance | 10⁻⁶ |
| Maximum stagnation iterations | 20 |

## Baseline Algorithms

The performance of IHGWSO is compared against:

- **GWO:** Standard Grey Wolf Optimizer.
- **PSO:** Standard Particle Swarm Optimization.
- **HGA-GWO:** Hybrid Genetic Algorithm–Grey Wolf Optimizer.

The comparative evaluation considers average response time, makespan, throughput, average resource utilization, degree of imbalance, and convergence.

## Source Code

The `src/` directory contains the Java implementation of the proposed algorithm and associated source files.


## Limitations

The experimental evaluation is conducted using CloudSim. Therefore, the findings are subject to simulation assumptions and do not fully represent the operational complexities of real cloud infrastructures.

The scalability results are limited to the evaluated configurations and workloads. Real-world validation, energy-aware optimization, and additional component-wise ablation experiments remain opportunities for future research.

## Research Publication

**IHGWSO / IMH_LB — Hybrid Metaheuristic Load Balancing for Cloud Computing**

Published in *ICT Express (2025)*.

Full publication details and DOI should be added to enable accurate citation.


## License

The source code is provided for academic review and research reference. Reuse and redistribution permissions are subject to the repository's applicable license and intellectual property restrictions.
