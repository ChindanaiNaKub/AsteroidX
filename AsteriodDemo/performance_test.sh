#!/bin/bash

echo "=== AsteroidX Performance Test ==="
echo "Testing game performance with optimizations..."
echo ""

# Compile the project
echo "1. Compiling project..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    exit 1
fi
echo "✅ Compilation successful"
echo ""

# Run the game with performance monitoring
echo "2. Starting game with performance monitoring..."
echo "   - Look for FPS counter in top-left corner"
echo "   - Target: 60 FPS"
echo "   - Press Ctrl+C to stop the game"
echo ""

# Run the game
mvn javafx:run

echo ""
echo "=== Performance Test Complete ==="
echo "If you saw consistent 60 FPS, the optimizations are working!"
echo "If you still see lag, check the FPS counter value." 