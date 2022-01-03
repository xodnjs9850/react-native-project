module.exports = {

  presets: ['module:metro-react-native-babel-preset'],

  plugins: [
    ['@babel/plugin-transform-flow-strip-types'],
    ['@babel/plugin-proposal-decorators', {'legacy': true}],
    ['@babel/plugin-proposal-class-properties', {'loose': true}],
    [
      'module-resolver',
      { 
        root: ['./src'],
        extensions: [
          '.js', 
          '.ios.ts', 
          '.android.ts',
          '.ts',
          '.json',
          '.ios.tsx',
          'android.tsx',
          '.tsx',
          '.jsx',
          ],
        alias: {
          '@': './src',
          '@components': './src/components',
          '@containers': './src/containers',
          '@assets': './src/assets',
          '@navigation': './src/navigation',
          '@screens': './src/screens',
          '@store': './src/store',
          '@utils': './src/utils',
          '@service': './src/service',
        },
      },
    ],
  ],
};

