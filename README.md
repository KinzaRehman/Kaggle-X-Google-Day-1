# Festive Effects Studio

Sophisticated and formal Android mobile interface showcasing high-fidelity interactive physics particle simulations using Jetpack Compose and modern Material Design 3.

This application is proudly built as a part of the **Kaggle X Google AI Intensive** program.

## Project Essence

Festive Effects Studio features an elegant dashboard styled under a deep, minimalist slate-obsidian visual theme. The interface provides immediate physical rendering feedback through custom canvas particle equations:

- **Snowflake Particle Simulator**: Renders realistic multi-branched hexagonal crystals with custom rotation speeds, continuous angular trajectories, and subtle lateral sinuous drifts. Runs for exactly 5 seconds upon trigger.
- **Balloon Particle Simulator**: Generates glossy, egg-shaped balloons with physical wiggling strings and realistic upward buoyancy drift. Runs for exactly 5 seconds upon trigger.

## Structural Design Pillars

- **Material 3 Ecosystem**: Employs an advanced obsidian dark theme, custom button state-tracking outlines, and a clean telemetry card displaying performance metrics in real time.
- **Double-Buffered State Engine**: Implements efficient frame-locked state updates using state loops linked to the hardware display refresh rate.
- **100% Native Drawing Core**: Bypasses heavy raster images to draw vector graphics programmatically inside Jetpack Compose `Canvas` coordinates, preserving device memory and maximizing GPU drawing rates.

---
*Developed for the Kaggle X Google AI Intensive Showcase.*
