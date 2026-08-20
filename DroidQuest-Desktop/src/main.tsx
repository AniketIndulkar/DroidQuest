import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { DroidQuestApp } from "@droidquest/react";
import { openUrl } from "@tauri-apps/plugin-opener";
import { desktopProgressStore } from "./adapters/desktopProgressStore";
import "@droidquest/react/styles.css";
import "./desktop.css";

const root = document.getElementById("root");

if (!root) {
  throw new Error("DroidQuest desktop root element is missing");
}

createRoot(root).render(
  <StrictMode>
    <DroidQuestApp
      progressStore={desktopProgressStore}
      storageDescription="the DroidQuest desktop app"
      openExternalUrl={openUrl}
    />
  </StrictMode>,
);
