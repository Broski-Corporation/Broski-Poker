# Broski Poker

A comprehensive Texas Hold'em poker game built with Java and libGDX, featuring both singleplayer and multiplayer modes with a dedicated server architecture.

## Game Features

- **Singleplayer Mode**: Play against AI opponents with varying difficulty levels
- **Multiplayer Mode**: Real-time online poker with friends using dedicated server
- **Complete Texas Hold'em Rules**: Pre-flop, flop, turn, river, and showdown phases
- **Professional UI**: Clean, intuitive interface with card animations and betting controls
- **User Authentication**: Secure login system with database integration
- **Cross-Platform**: Runs on Windows, macOS, and Linux

## Demos

- [Singleplayer Demo](https://www.youtube.com/watch?v=e3jgQ82BAzc)
- **Multiplayer Demos** (recorded from 2 different perspectives):
  - [Creating Table Point of View](https://www.youtube.com/watch?v=0ODtfTlZpZk)
  - [Joining Table Point of View](https://www.youtube.com/watch?v=SnOqhq_sypk)

## Architecture

The game follows the **Model-View-Controller (MVC)** design pattern:

- **Model**: `PokerGame.java` - Game logic and state management
- **View**: `GameRenderer.java` - UI rendering and visual components  
- **Controller**: `GameController.java` - Input handling and game flow control

### Key Components

- **Main.java**: Application entry point and lifecycle management
- **PokerGame.java**: Core game logic, betting rounds, hand evaluation
- **GameRenderer.java**: Rendering engine with libGDX
- **PokerServer.java**: Dedicated multiplayer server
- **ClientConnection.java**: Network client for multiplayer communication

## Technology Stack

- **Java 21**: Primary programming language
- **libGDX 1.13.1**: Game development framework
- **KryoNet 2.22.0-RC1**: Networking library for multiplayer
- **PostgreSQL**: Database for user management
- **BCrypt**: Password hashing and security
- **Gradle**: Build automation and dependency management

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 7.0+
- PostgreSQL (for database features)

### Building the Game

```bash
# Clone the repository
git clone https://github.com/Broski-SRL/Broski-Poker.git
cd Broski-Poker

# Build the project
./gradlew build

# Run the game
./gradlew lwjgl3:run
```

## Multiplayer Server

The multiplayer functionality is powered by a dedicated server that we compiled as a standalone JAR and deployed to a VPS for 24/7 availability.

### Server Architecture

- **Centralized Game State**: Server maintains authoritative game state
- **Real-time Communication**: TCP-based networking with KryoNet
- **Table Management**: Support for multiple concurrent poker tables
- **Player Authentication**: Secure login and session management

### Server Deployment

```bash
# Build the server JAR
./gradlew core:serverJar

# Deploy to server
./deploy-to-server.sh

# View server logs
./deploy-to-server.sh logs
```

The server runs continuously on our VPS, ensuring stable multiplayer connectivity for all players.

### Network Protocol

The game uses a custom protocol with serialized objects:
- `GameStateUpdate`: Synchronizes game state between server and clients
- `PlayerAction`: Handles betting actions (check, call, raise, fold)
- `TableManagement`: Create/join table functionality
- `LoginRequest/Response`: User authentication

## Game Rules

**Texas Hold'em Poker Rules Implemented:**

1. **Pre-flop**: Each player receives 2 hole cards
2. **Flop**: 3 community cards dealt
3. **Turn**: 4th community card dealt  
4. **River**: 5th community card dealt
5. **Showdown**: Best 5-card hand wins

**Betting Actions:**
- **Check**: Pass without betting (if no bet to call)
- **Call**: Match the current bet
- **Raise**: Increase the bet amount
- **Fold**: Discard hand and exit round

**Default Settings:**
- Small Blind: 50 chips
- Big Blind: 100 chips
- Starting Chips: 10,000

## Software Development Tasks

### Project Management
- [User Stories](https://github.com/orgs/Broski-Corporation/projects/3) - Agile development tracking
- [Diagrams](https://github.com/Broski-Corporation/Broski-Poker/actions/runs/15681976011) - System architecture documentation

### Source Control
- [Branch Creation](https://github.com/Broski-SRL/Broski-Poker/branches) - Feature branch workflow
- [Merge/Rebase](https://github.com/Broski-Corporation/Broski-Poker/network) - Git workflow documentation  
- [Pull Requests](https://github.com/Broski-SRL/Broski-Poker/pulls?q=is%3Apr+is%3Aclosed) - Code review process
- [Commit History](https://github.com/Broski-SRL/Broski-Poker/commits/main/) - Development progression (180+ commits)

### Quality Assurance
- [Automated Tests](https://github.com/Broski-Corporation/Broski-Poker/actions) - Unit and integration testing using GitHub Actions
- [Bug Resolution](https://github.com/Broski-SRL/Broski-Poker/pull/33) - Issue tracking and fixes via PR

### Coding Standards

#### Java Code Conventions
- **Package Naming**: Reverse domain notation (`io.github.broskipoker.*`)
- **Class Naming**: PascalCase for all class names (`PokerGame`, `TableManager`, `GameController`)
- **Method Naming**: camelCase for all method names (`generateUniqueTableCode`, `addPlayer`, `getCurrentBet`)
- **Variable Naming**: camelCase for instance variables and local variables (`currentBet`, `smallBlind`, `connectionToCode`)
- **Constant Naming**: UPPER_SNAKE_CASE for static final constants (`HUMAN_PLAYER_INDEX`, `BOT_THINKING_TIME`, `CARD_WIDTH`)

#### Field and Access Modifiers
- **Encapsulation**: Consistent use of `private final` for immutable fields (`private final String name`, `private final Random random`)
- **Static Fields**: Proper use of `static final` for shared constants and utilities
- **Access Control**: Appropriate use of `public`, `private`, and `synchronized` modifiers
- **Immutability**: Defensive copying in getter methods (e.g., `new ArrayList<>(connections)`)

#### Method Organization
- **Getter/Setter Pattern**: Consistent implementation across model classes
- **Synchronization**: Thread-safe operations using `synchronized` keyword for concurrent access

#### Code Structure
- **Enum Usage**: Well-structured enums with appropriate values (`BotStrategy`, `GameState`, `PlayerAction`)
- **Resource Management**: Proper disposal patterns implementing `Disposable` interface

#### Documentation Standards
- **Inline Comments**: Strategic use of comments to explain complex game logic
- **Code Organization**: Logical grouping of related methods and clear class responsibilities

### Design Patterns
- **MVC**: Model (PokerGame.java), View (GameRenderer.java), Controller (GameController.java)
- **State**: PokerGame.GameState transitions between game phases (BETTING_PRE_FLOP, FLOP, TURN, etc.)
- **Observer**: BettingUI.java updates UI based on PokerGame state changes
- **Singleton**: SoundManager.java, FontManager.java for resource management
- **Template Method**: RegisterDialog.java/LoginDialog.java extending Dialog with specific behaviors
- **Factory Method**: FontManager.java creates font instances on demand
- **Callback**: onLoginSuccess Runnable for post-authentication logic
- **Facade**: Menu.java simplifies UI component interactions

### AI-Assisted Development

During development, we relied on several AI tools to streamline coding, debugging, and learning complex frameworks:

**GitHub Copilot** (in IntelliJ) was used for code autocompletion and rapid boilerplate generation, especially for Java and LibGDX integration. It helped speed up routine tasks and reduced manual coding effort.

**GitHub Copilot Agent Mode** proved invaluable for complex debugging sessions, particularly when troubleshooting backend-frontend integration issues. It helped resolve problems with:
- Server-client communication synchronization between `PokerServer.java` and UI components
- Network message handling and state updates in multiplayer gameplay
- Threading issues in real-time game state propagation

**Claude Sonnet** delivered the best overall performance. It was particularly effective for:
- Debugging complex poker game logic and state management
- Explaining how specific LibGDX components work (rendering, input handling, asset management)
- Refactoring and improving existing code architecture
- **Server deployment guidance** - helping us successfully deploy the multiplayer server to our VPS infrastructure
- **AI bot development** - providing strategic insights and implementation guidance for the `PokerBot.java` decision-making algorithms and behavioral patterns


## Project Structure

```
Broski-Poker/
├── core/                          # Core game logic
│   ├── src/main/java/io/github/broskipoker/
│   │   ├── game/                  # Game mechanics (PokerGame, Player, Card, etc.)
│   │   ├── ui/                    # User interface components
│   │   ├── server/                # Multiplayer server code
│   │   ├── shared/                # Network protocol classes
│   │   └── utils/                 # Utility classes
│   └── build.gradle               # Core module dependencies
├── lwjgl3/                        # Desktop launcher
│   └── build.gradle               # Desktop-specific build config
├── assets/                        # Game assets (images, sounds, fonts)
├── gradle.properties              # Project configuration
└── build.gradle                   # Root build configuration
```

## Assets & UI

- **Card Graphics**: High-quality playing card sprites
- **Table Design**: Realistic poker table interface
- **Animations**: Smooth card dealing and chip rendering
- **Sound Effects**: Authentic casino atmosphere
- **Fonts**: Custom typography for professional appearance

## Configuration

Key configuration files:
- `gradle.properties`: Build settings and versions
- `core/build.gradle`: Dependencies and server JAR configuration
- `lwjgl3/build.gradle`: Desktop platform settings

## License

This project is developed as part of academic coursework for software engineering practices.

## Team

**Broski Corporation**
- Game Development
- Server Infrastructure  
- UI/UX Design
- Quality Assurance
