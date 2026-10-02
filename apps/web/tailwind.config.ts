import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./app/**/*.{ts,tsx}",
    "./src/**/*.{ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        graphite: "#171717",
        canvas: "#f7f4ef",
        ember: "#b42318",
      },
    },
  },
  plugins: [],
};

export default config;
