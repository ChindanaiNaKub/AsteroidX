# AsteroidX 🚀

A modern JavaFX-based space shooter game featuring dynamic gameplay, multiple weapon systems, AI assistance, and boss battles.

![AsteroidX Game](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-3.9.9-red?style=for-the-badge&logo=apache-maven)

## 🎮 Game Overview

AsteroidX is an action-packed space shooter where you pilot a ship through asteroid fields, battle enemy vessels, and face challenging boss encounters. The game features smooth JavaFX graphics, dynamic sound effects, and multiple gameplay modes.

### ✨ Key Features

- **Dynamic Space Combat**: Navigate through asteroid fields while engaging enemy ships
- **Multiple Weapon Systems**: Choose from different bullet types for various combat strategies
- **AI Assistance**: Activate Ship AI mode for automated gameplay assistance
- **Boss Battles**: Face challenging boss encounters at higher scores
- **Drone Support**: Summon drone companions for tactical advantage
- **Progressive Difficulty**: Increasingly challenging gameplay as you advance
- **Sound Effects**: Immersive audio experience with laser, explosion, and thrust sounds
- **Score System**: Track your progress and compete for high scores

## 🛠️ Prerequisites

Before running AsteroidX, ensure you have the following installed:

- **Java 20 or higher** (OpenJDK or Oracle JDK)
- **Maven 3.6+** (for dependency management and building)
- **Git** (for cloning the repository)

### Installing Prerequisites

#### On Ubuntu/Debian:
```bash
sudo apt update
sudo apt install openjdk-21-jdk maven git
```

#### On Fedora/RHEL:
```bash
sudo dnf install java-21-openjdk-devel maven git
```

#### On macOS (using Homebrew):
```bash
brew install openjdk@21 maven git
```

#### On Windows:
- Download and install [OpenJDK 21](https://adoptium.net/)
- Download and install [Maven](https://maven.apache.org/download.cgi)
- Download and install [Git](https://git-scm.com/)

## 🚀 Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd AsteroidX/AsteriodDemo
   ```

2. **Compile the project:**
   ```bash
   mvn clean compile
   ```

3. **Run the game:**
   ```bash
   mvn javafx:run
   ```

### Alternative: Create Executable JAR

To create a standalone executable JAR file:

```bash
mvn clean package
java -jar target/AsteriodDemo-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 🎯 Game Controls

### Movement Controls
- **↑ (Up Arrow)**: Move ship upward
- **↓ (Down Arrow)**: Move ship downward
- **← (Left Arrow)**: Move ship left
- **→ (Right Arrow)**: Move ship right

### Combat Controls
- **Spacebar**: Fire default bullets
- **Z**: Switch to Shuriken mode
- **X**: Switch to Pulse mode
- **F**: Return to default bullet mode

### Special Features
- **F1**: Activate Ship AI Mode (automated assistance)
- **F2**: Deactivate Ship AI Mode
- **C**: Cheat mode - Skip to boss stage (for testing)

## 🎮 Gameplay Mechanics

### Objective
Survive as long as possible while destroying asteroids and enemy ships to achieve the highest score.

### Scoring System
- **Asteroid Destruction**: 1 point per asteroid
- **Enemy Ship Destruction**: 5 points per enemy ship
- **Boss Defeat**: 50+ points (varies by boss difficulty)

### Game Progression
1. **Normal Mode**: Start with basic asteroids and occasional enemy ships
2. **AI Mode**: Automatically activates at higher scores for assistance
3. **Boss Stage**: Triggered at score 17+ or using cheat mode
4. **Drone Support**: Available with cooldown periods

### Lives System
- Start with 3 lives
- Lose a life when hit by asteroids or enemy fire
- Game over when all lives are depleted

## 🏗️ Project Structure

```
AsteroidX/
├── AsteriodDemo/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/se233/asterioddemo/
│   │   │   │   ├── AsteroidGame.java          # Main game class
│   │   │   │   ├── PlayerShip.java            # Player ship logic
│   │   │   │   ├── Asteroid.java              # Asteroid entities
│   │   │   │   ├── EnemyShip.java             # Enemy ship AI
│   │   │   │   ├── Boss.java                  # Boss battle system
│   │   │   │   ├── Bullet.java                # Projectile system
│   │   │   │   ├── GameEntityManager.java     # Game state management
│   │   │   │   ├── ShipAI.java                # AI assistance system
│   │   │   │   ├── Drone.java                 # Drone companion
│   │   │   │   └── exception/                 # Custom exceptions
│   │   │   └── resources/
│   │   │       ├── sprite/                    # Game graphics
│   │   │       ├── sounds/                    # Audio files
│   │   │       └── logging.properties         # Logging configuration
│   │   └── test/                              # Unit tests
│   ├── pom.xml                                # Maven configuration
│   └── README.md                              # This file
```

## 🧪 Testing

Run the test suite to ensure everything is working correctly:

```bash
mvn test
```

The project includes comprehensive unit tests for:
- Character movement and actions
- Game scoring mechanics
- Input controller functionality

## 🔧 Development

### Building from Source

1. **Fork and clone the repository**
2. **Install dependencies:**
   ```bash
   mvn dependency:resolve
   ```
3. **Compile:**
   ```bash
   mvn clean compile
   ```
4. **Run tests:**
   ```bash
   mvn test
   ```

### Key Dependencies

- **JavaFX 21.0.2**: UI framework and graphics
- **JUnit 5.10.2**: Testing framework
- **Mockito 4.0.0**: Mocking for tests
- **Log4j 2.24.0**: Logging system

## 🐛 Troubleshooting

### Common Issues

**"Maven command not found"**
- Ensure Maven is installed and added to your PATH
- On Linux: `sudo dnf install maven` (Fedora) or `sudo apt install maven` (Ubuntu)

**"JavaFX runtime components are missing"**
- The project includes JavaFX dependencies automatically
- Ensure you're using Java 20+ and Maven 3.6+

**"MediaException: Could not create player"**
- This is a known issue with audio playback on some systems
- Game functionality is not affected, only sound effects

**"Module name component should avoid terminal digits"**
- This is a warning about the module name format
- Does not affect game functionality

### Performance Optimization

- Close other resource-intensive applications while playing
- Ensure your graphics drivers are up to date
- On Linux, consider using hardware acceleration if available

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Support

If you encounter any issues or have questions:

1. Check the troubleshooting section above
2. Review existing issues in the repository
3. Create a new issue with detailed information about your problem

## 🎯 Future Enhancements

- [ ] Multiplayer support
- [ ] Power-up system
- [ ] Different ship types
- [ ] Level progression system
- [ ] High score leaderboard
- [ ] Customizable controls
- [ ] Additional boss types
- [ ] Enhanced visual effects

## ⚡ Performance Optimizations

The game has been optimized for smooth 60 FPS gameplay with the following improvements:

- **Frame Rate Control**: Consistent 60 FPS target
- **Background Rendering**: Optimized boss stage background (80% reduction in overhead)
- **Frame-Based Spawning**: Eliminated thread synchronization issues
- **Grid Removal**: Reduced unnecessary rendering overhead
- **Performance Monitoring**: Real-time FPS counter

For detailed optimization information, see [PERFORMANCE_OPTIMIZATIONS.md](AsteriodDemo/PERFORMANCE_OPTIMIZATIONS.md).

---

**Enjoy playing AsteroidX!** 🚀💫

