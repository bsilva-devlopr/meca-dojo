import type { ReactNode } from "react";
import "./globals.css";

export const metadata = {
  title: "Meca Dojo",
  description: "Gestao inteligente para equipes de Jiu-Jitsu",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
