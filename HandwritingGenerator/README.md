# Handwriting Generator

A Java Swing application that simulates handwriting by rendering text with unique, randomized variations each time. Built for BlueJ compatibility.

## Features

- **Unique Output**: Each rendering is different, even for the same text
- **Random Transformations**: Uses `AffineTransform` to apply:
  - Random shear (slant)
  - Random rotation (slight tilt)
  - Random position jitter
  - Random scale variations
- **Visual Effects**: Adds paper texture and ink spots for realistic appearance
- **Simple GUI**: Text input field, "Write" button, and display panel

## How to Open and Run in BlueJ

### Opening the Project

1. Launch BlueJ
2. Click **Project** → **Open Project**
3. Navigate to the `HandwritingGenerator` folder
4. Click **Open**

### Running the Application

#### Method 1: Using the Main class
1. In the BlueJ class diagram, right-click on the **Main** class
2. Select **void main(String[] args)**
3. Click **OK** (leave the args empty)
4. The Handwriting Generator window will appear

#### Method 2: Creating an instance
1. Right-click on the **Main** class
2. Select **new Main()**
3. The application window will open automatically

### Using the Application

1. Type your text in the input field at the top
2. Click the **"Write"** button (or press Enter)
3. Your text will appear in handwriting style below
4. Type the same text again and click "Write" - notice how the output is different each time!

## Technical Details

### Classes

- **Main.java**: Entry point with JFrame and GUI components (text field, button, panel)
- **HandwritingPanel.java**: Custom JPanel that renders text with handwriting effects

### How Uniqueness is Achieved

For each character drawn, the program applies:
- **Position Jitter**: Random x/y offset (±3-4 pixels)
- **Rotation**: Random tilt (±4.3 degrees)
- **Shear**: Random slant for italic-like effect
- **Scale**: Random size variation (90-110% of base size)
- **Spacing**: Random gaps between characters
- **Color**: Slight variations in darkness
- **Effects**: Random paper texture lines and ink spots

The random number generator is reseeded with `System.nanoTime()` each time you click "Write", ensuring true uniqueness.

## System Requirements

- Java 8 or higher
- BlueJ 4.0 or higher
- Any operating system that supports Java Swing

## Customization

You can modify these parameters in `HandwritingPanel.java`:

- **Base font size**: Line 64 - `Font baseFont = new Font("SansSerif", Font.PLAIN, 36);`
- **Position jitter**: Lines 105-106 - Adjust the multiplication factor
- **Rotation range**: Line 109 - Adjust the 0.15 value
- **Shear range**: Lines 112-113 - Adjust the 0.2 and 0.1 values
- **Scale range**: Line 116 - Adjust the 0.9 to 1.1 range

## Example Use Cases

- Create unique handwritten notes
- Generate varied text samples for design mockups
- Demonstrate randomization in computer graphics
- Learn about Java 2D transformations and custom painting

## License

This is a school project and is provided as-is for educational purposes.
