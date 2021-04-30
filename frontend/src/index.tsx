import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { BrowserRouter as Router } from 'react-router-dom';
import { ThemeProvider } from 'styled-components';
import { App } from './App';
import './index.css';
import { initStore } from './state';
import { Theme } from './theme';


const appStore = initStore();

ReactDOM.render(
  <React.StrictMode>
    <ThemeProvider theme={Theme}>
      <Provider store={appStore}>
        <Router>
          <App />
        </Router>
      </Provider>
    </ThemeProvider>
  </React.StrictMode>,
  document.getElementById('root')
);
