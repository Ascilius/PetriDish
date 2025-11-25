## Petri Dish

### Controls:
 - Space: Pause
 - Right: Step
 - Escape: Exit
 - F3: Toggle debug UI
 - R: Reset
 - P: Pause

### todo
 - add agar "growth"
 - remove entity minimums and not have the ecosystem collapse?
 - optimize
    - GPU usage
 - reimplement FPS history
 - saving/loading system
 - zoom/translate camera
 - review access modifiers

### v3 (2025-11-25)
 - overhauled Brain
    - now that I know how neural networks function, I can implement them correctly
    - replaced the two turning features with one
 - increased ecosystem size (reduced organism speed/range)
 - added cannibalism: dead organisms now spwan agar proportional to remaining energy

### v2 (2024-11-07)
 - split organisms into herbivores and carnivores
    - herbivores (blue) eat only agar (previously referred to as food)
    - carnivores (red) eat herbivores
 - changed sight from entity count to total color
 - neural networks expanded to handle new inputs
 - speed is now based off of neural network brain and logistic growth curve

### v1.1
 - added hunger
 - optimized neural network math

### v1 (2024-11-06)
 - organisms
    - added sight
    - added neural network processing
    - added eating functionality
