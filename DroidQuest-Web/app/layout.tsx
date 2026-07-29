import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  metadataBase: new URL("http://localhost:3000"),
  title: "DroidQuest — Learn Android by building",
  description:
    "A local-first, gamified path from Kotlin fundamentals to Android platform expertise.",
  openGraph: {
    title: "DroidQuest",
    description: "Learn Android by building",
    images: [{ url: "/og.png", width: 1734, height: 908, alt: "DroidQuest — Learn Android by building" }],
  },
  twitter: {
    card: "summary_large_image",
    title: "DroidQuest",
    description: "Learn Android by building",
    images: ["/og.png"],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        {children}
      </body>
    </html>
  );
}
