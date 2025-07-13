// Game constants
const GRID_SIZE = 20;
const CELL_SIZE = 20;
const INITIAL_SNAKE_SPEED = 100; // ms per move
const SPEED_INCREASE = 0.95; // speed multiplier per food eaten

// Game variables
let canvas, ctx;
let snake = [];
let food = {};
let direction = 'right';
let nextDirection = 'right';
let score = 0;
let speed = INITIAL_SNAKE_SPEED;
let lastUpdate = 0;
let gameRunning = false;
let animationFrameId;

// UI elements
const scoreElement = document.getElementById('score');
const finalScoreElement = document.getElementById('finalScore');
const startScreen = document.getElementById('startScreen');
const gameOverScreen = document.getElementById('gameOverScreen');
const startButton = document.getElementById('startButton');
const restartButton = document.getElementById('restartButton');

// Initialize the game
function init() {
    canvas = document.getElementById('gameCanvas');
    ctx = canvas.getContext('2d');

    // Set canvas size
    canvas.width = GRID_SIZE * CELL_SIZE;
    canvas.height = GRID_SIZE * CELL_SIZE;

    // Event listeners
    window.addEventListener('keydown', handleKeyPress);
    startButton.addEventListener('click', startGame);
    restartButton.addEventListener('click', startGame);

    // Initial game state
    resetGame();

    // Start the game loop
    render();
}

// Reset game state
function resetGame() {
    // Initialize snake
    snake = [
        {x: 10, y: 10},
        {x: 9, y: 10},
        {x: 8, y: 10}
    ];

    direction = 'right';
    nextDirection = 'right';
    score = 0;
    speed = INITIAL_SNAKE_SPEED;

    // Generate first food
    generateFood();

    // Update UI
    scoreElement.textContent = score;
}

// Start the game
function startGame() {
    resetGame();
    gameRunning = true;
    startScreen.classList.remove('active');
    gameOverScreen.classList.remove('active');
    lastUpdate = performance.now();
    render();
}

// End the game
function gameOver() {
    gameRunning = false;
    finalScoreElement.textContent = score;
    gameOverScreen.classList.add('active');
}

// Generate food at random position
function generateFood() {
    // Ensure food doesn't spawn on snake
    let validPosition = false;
    let newFood;

    while (!validPosition) {
        newFood = {
            x: Math.floor(Math.random() * GRID_SIZE),
            y: Math.floor(Math.random() * GRID_SIZE)
        };

        validPosition = !snake.some(segment => 
            segment.x === newFood.x && segment.y === newFood.y
        );
    }

    food = newFood;
}

// Handle keyboard input
function handleKeyPress(e) {
    if (!gameRunning) return;

    switch (e.key) {
        case 'ArrowUp':
        case 'w':
        case 'W':
            if (direction !== 'down') nextDirection = 'up';
            break;
        case 'ArrowDown':
        case 's':
        case 'S':
            if (direction !== 'up') nextDirection = 'down';
            break;
        case 'ArrowLeft':
        case 'a':
        case 'A':
            if (direction !== 'right') nextDirection = 'left';
            break;
        case 'ArrowRight':
        case 'd':
        case 'D':
            if (direction !== 'left') nextDirection = 'right';
            break;
    }
}

// Update game state
function update(currentTime) {
    if (!gameRunning) return;

    // Calculate time since last update
    const deltaTime = currentTime - lastUpdate;

    if (deltaTime > speed) {
        // Update direction
        direction = nextDirection;

        // Get snake head
        const head = {...snake[0]};

        // Move head based on direction
        switch (direction) {
            case 'up':
                head.y -= 1;
                break;
            case 'down':
                head.y += 1;
                break;
            case 'left':
                head.x -= 1;
                break;
            case 'right':
                head.x += 1;
                break;
        }

        // Check for wall collision
        if (head.x < 0 || head.x >= GRID_SIZE || head.y < 0 || head.y >= GRID_SIZE) {
            gameOver();
            return;
        }

        // Check for self collision
        if (snake.some(segment => segment.x === head.x && segment.y === head.y)) {
            gameOver();
            return;
        }

        // Add new head
        snake.unshift(head);

        // Check for food collision
        if (head.x === food.x && head.y === food.y) {
            // Increase score
            score += 10;
            scoreElement.textContent = score;

            // Increase speed
            speed *= SPEED_INCREASE;

            // Generate new food
            generateFood();
        } else {
            // Remove tail if no food eaten
            snake.pop();
        }

        lastUpdate = currentTime;
    }
}

// Draw the game
function render() {
    if (!gameRunning && animationFrameId) {
        cancelAnimationFrame(animationFrameId);
        return;
    }

    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Draw grid
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.05)';
    ctx.lineWidth = 0.5;

    for (let x = 0; x <= GRID_SIZE; x++) {
        ctx.beginPath();
        ctx.moveTo(x * CELL_SIZE, 0);
        ctx.lineTo(x * CELL_SIZE, canvas.height);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(0, x * CELL_SIZE);
        ctx.lineTo(canvas.width, x * CELL_SIZE);
        ctx.stroke();
    }

    // Draw food with pulsing animation
    const pulse = (Math.sin(Date.now() / 200) * 0.2) + 0.8;
    const foodSize = CELL_SIZE * 0.8 * pulse;
    const foodOffset = (CELL_SIZE - foodSize) / 2;

    ctx.beginPath();
    ctx.fillStyle = '#e94560';
    ctx.arc(
        food.x * CELL_SIZE + CELL_SIZE / 2,
        food.y * CELL_SIZE + CELL_SIZE / 2,
        foodSize / 2,
        0,
        Math.PI * 2
    );
    ctx.fill();

    // Draw snake with gradient and smooth movement
    snake.forEach((segment, index) => {
        // Gradient from head (bright) to tail (dark)
        const hue = 351; // Red hue
        const saturation = 80;
        const lightness = 60 - (index / snake.length * 40);
        ctx.fillStyle = `hsl(${hue}, ${saturation}%, ${lightness}%)`;

        // Slightly rounded corners for smoother look
        const borderRadius = CELL_SIZE * 0.2;
        const posX = segment.x * CELL_SIZE;
        const posY = segment.y * CELL_SIZE;

        ctx.beginPath();
        ctx.moveTo(posX + borderRadius, posY);
        ctx.lineTo(posX + CELL_SIZE - borderRadius, posY);
        ctx.quadraticCurveTo(posX + CELL_SIZE, posY, posX + CELL_SIZE, posY + borderRadius);
        ctx.lineTo(posX + CELL_SIZE, posY + CELL_SIZE - borderRadius);
        ctx.quadraticCurveTo(posX + CELL_SIZE, posY + CELL_SIZE, posX + CELL_SIZE - borderRadius, posY + CELL_SIZE);
        ctx.lineTo(posX + borderRadius, posY + CELL_SIZE);
        ctx.quadraticCurveTo(posX, posY + CELL_SIZE, posX, posY + CELL_SIZE - borderRadius);
        ctx.lineTo(posX, posY + borderRadius);
        ctx.quadraticCurveTo(posX, posY, posX + borderRadius, posY);
        ctx.closePath();
        ctx.fill();

        // Draw eyes on head
        if (index === 0) {
            const eyeSize = CELL_SIZE * 0.15;
            const eyeOffsetX = direction === 'left' ? -0.2 : 
                              direction === 'right' ? 0.2 : 0;
            const eyeOffsetY = direction === 'up' ? -0.2 :
                             direction === 'down' ? 0.2 : 0;

            // Left eye
            ctx.beginPath();
            ctx.fillStyle = 'white';
            ctx.arc(
                posX + CELL_SIZE * (0.3 + eyeOffsetX),
                posY + CELL_SIZE * (0.3 + eyeOffsetY),
                eyeSize,
                0,
                Math.PI * 2
            );
            ctx.fill();

            // Right eye
            ctx.beginPath();
            ctx.arc(
                posX + CELL_SIZE * (0.7 + eyeOffsetX),
                posY + CELL_SIZE * (0.3 + eyeOffsetY),
                eyeSize,
                0,
                Math.PI * 2
            );
            ctx.fill();
        }
    });

    // Request next frame
    animationFrameId = requestAnimationFrame((timestamp) => {
        update(timestamp);
        render();
    });
}

// Initialize the game when the page loads
window.onload = init;
