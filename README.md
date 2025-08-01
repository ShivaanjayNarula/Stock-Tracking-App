# Stock Tracking App Java

A modern JavaFX-based stock market data visualization application that provides real-time stock data analysis with interactive candlestick charts and local data persistence.

## 📊 Features

### Core Functionality
- **Real-time Stock Data**: Fetch live stock data from Alpha Vantage API
- **Interactive Candlestick Charts**: Custom-built candlestick chart visualization with hover effects
- **Symbol History**: Quick access to recently searched stock symbols
- **Data Persistence**: Local SQLite database for storing historical stock data
- **Rate Limiting**: Built-in API rate limiting to respect service limits
- **Error Handling**: Comprehensive error handling for network and API issues

### User Interface
- **Modern UI Design**: Clean, professional interface with Material Design-inspired styling
- **Responsive Layout**: Adaptive design that works on different screen sizes
- **Interactive Charts**: Hover effects, tooltips, and dynamic scaling
- **Input Validation**: Real-time validation for stock symbol format
- **Status Feedback**: Clear status messages for user actions and errors

### Technical Features
- **JavaFX Graphics**: Custom candlestick chart implementation
- **Database Integration**: SQLite for local data storage
- **HTTP Client**: OkHttp for API communication
- **JSON Processing**: Native JSON handling for API responses
- **Maven Build System**: Standardized dependency management

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Internet connection for API access

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/ShivaanjayNarula/Stock-Tracking-App
   cd Stock-Tracking-App
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```
   
## 📖 Usage Guide

### Getting Started
1. Launch the application
2. Enter a stock symbol (e.g., AAPL, MSFT, GOOGL)
3. Click "Show Chart" to fetch and display data
4. Use the history dropdown to quickly access recent symbols

### Features in Detail

#### Stock Symbol Input
- Enter 1-5 letter stock symbols in uppercase
- Real-time validation with visual feedback
- Auto-completion from recent searches

#### Chart Interaction
- **Hover Effects**: Candlesticks scale and highlight on hover
- **Tooltips**: Detailed price information on hover
- **Color Coding**: Green for bullish (close > open), red for bearish
- **Auto-scaling**: Y-axis automatically adjusts to data range

#### Data Management
- **Local Storage**: All fetched data is stored in SQLite database
- **Offline Mode**: Sample data generation when API is unavailable
- **Rate Limiting**: Automatic API call limiting (5 calls/minute)

## 🏗️ Architecture

### Project Display

<img width="2886" height="1474" alt="image" src="https://github.com/user-attachments/assets/99d19593-92e2-4292-8fd9-9c29a0a1b87c" />


### Project Structure
```
src/main/java/com/stocktracker/
├── api/
│   └── ApiClient.java          # API communication layer
├── database/
│   ├── DatabaseManager.java     # Database initialization
│   └── daos/
│       ├── StockDAO.java       # Stock data access
│       └── UserDAO.java        # User data access
├── models/
│   └── StockData.java          # Data model
├── ui/
│   ├── MainApp.java            # Main application UI
│   └── charts/
│       └── CandlestickChart.java # Custom chart implementation
└── Main.java                   # Entry point
```

### Key Components

#### API Layer (`ApiClient.java`)
- **HTTP Communication**: Uses OkHttp for API requests
- **Rate Limiting**: Built-in rate limiting mechanism
- **Error Handling**: Comprehensive error detection and reporting
- **Data Parsing**: JSON response parsing for multiple data formats

#### Database Layer
- **SQLite Database**: Local data persistence
- **Table Schema**: Users, stock_data, and alerts tables
- **DAO Pattern**: Data Access Objects for clean data operations

#### UI Layer
- **JavaFX Application**: Modern desktop UI framework
- **Custom Charts**: Proprietary candlestick chart implementation
- **Responsive Design**: Adaptive layout and styling
- **Event Handling**: Comprehensive user interaction management

#### Data Models
- **StockData**: Core data structure for stock information
- **Immutable Design**: Thread-safe data objects
- **Time-based**: LocalDateTime for precise timestamp handling

## 🔧 Configuration

### API Configuration
The application uses Alpha Vantage API for stock data. Currently configured with demo key:
```java
private static final String API_KEY = "demo"; // Using demo key for testing
```

To use your own API key:
1. Sign up at [Alpha Vantage](https://www.alphavantage.co/)
2. Replace the API_KEY constant in `ApiClient.java`
3. Rebuild the application

### Database Configuration
- **Database File**: `stocks.db` (SQLite)
- **Location**: Project root directory
- **Auto-initialization**: Tables created automatically on first run

## 📦 Dependencies

### Core Dependencies
- **JavaFX 20.0.1**: UI framework
- **OkHttp 4.11.0**: HTTP client
- **SQLite JDBC 3.42.0.0**: Database driver
- **JSON 20231013**: JSON processing

### Build Configuration
- **Java Version**: 17
- **Maven Plugin**: JavaFX Maven Plugin 0.0.8
- **Encoding**: UTF-8

## 🛠️ Development

### Building from Source
```bash
# Clean and compile
mvn clean compile

# Run tests (if available)
mvn test

# Create executable JAR
mvn clean package

# Run with JavaFX plugin
mvn javafx:run
```

### Development Setup
1. **IDE Configuration**: Import as Maven project
2. **JavaFX Setup**: Ensure JavaFX modules are properly configured
3. **Database**: SQLite database will be created automatically
4. **API Key**: Replace demo key with your Alpha Vantage API key

### Code Style
- **Package Structure**: Follows standard Java conventions
- **Naming**: Clear, descriptive class and method names
- **Documentation**: Comprehensive inline comments
- **Error Handling**: Try-catch blocks with meaningful error messages

## 🐛 Troubleshooting

### Common Issues

#### API Rate Limiting
- **Symptom**: "API rate limit exceeded" error
- **Solution**: Wait 1 minute between requests (5 calls/minute limit)
- **Workaround**: Application generates sample data when API is unavailable

#### Database Issues
- **Symptom**: SQLException on startup
- **Solution**: Ensure write permissions in project directory
- **Check**: Verify `stocks.db` file is not locked

#### JavaFX Issues
- **Symptom**: "JavaFX runtime components are missing"
- **Solution**: Ensure JavaFX modules are included in classpath
- **Alternative**: Use `mvn javafx:run` instead of direct execution

#### Network Issues
- **Symptom**: "Network error" messages
- **Solution**: Check internet connection
- **Fallback**: Application will generate sample data

### Debug Mode
Enable debug output by checking console logs:
```bash
 mvn javafx:run -Debug=true
```

## 📈 Future Enhancements

### Planned Features
- **Real-time Updates**: WebSocket integration for live data
- **Portfolio Management**: Multi-stock tracking and analysis
- **Technical Indicators**: Moving averages, RSI, MACD
- **Export Functionality**: CSV/PDF export of chart data
- **User Authentication**: Login system with user preferences
- **Alerts System**: Price-based notification system

### Technical Improvements
- **Caching Layer**: Redis integration for better performance
- **Microservices**: API gateway and service separation
- **Containerization**: Docker support for easy deployment
- **Testing**: Comprehensive unit and integration tests

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check the troubleshooting section above
- Review the code documentation

## 🙏 Acknowledgments

- **Alpha Vantage**: Stock market data API
- **JavaFX**: UI framework
- **SQLite**: Database engine
- **OkHttp**: HTTP client library

---
