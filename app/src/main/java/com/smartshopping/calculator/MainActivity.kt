package com.stellasecret.smartshoppingcalculator

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.stellasecret.smartshoppingcalculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupSwipeRefresh()

        // Restore state after rotation, otherwise load fresh
        if (savedInstanceState != null) {
            binding.webView.restoreState(savedInstanceState)
        } else {
            binding.webView.loadUrl("file:///android_asset/index.html")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val wv = binding.webView
        wv.settings.apply {
            javaScriptEnabled        = true
            domStorageEnabled        = true
            allowFileAccess          = true
            allowContentAccess       = true
            useWideViewPort          = true
            loadWithOverviewMode     = true
            setSupportZoom(false)
            builtInZoomControls      = false
            displayZoomControls      = false
            // Disable text size adjustment so the app controls its own layout
            textZoom                 = 100
        }

        // Inject dark-mode class before the page renders
        wv.addJavascriptInterface(AndroidBridge(this), "AndroidBridge")

        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                injectDarkModeIfNeeded()
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                binding.progressBar.visibility = View.GONE
            }
        }

        wv.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress < 100) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress   = newProgress
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.webView.reload()
        }
        // Disable pull-to-refresh if the WebView is scrolled down
        binding.webView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            binding.swipeRefresh.isEnabled = (scrollY == 0)
        }
    }

    /** Sync the HTML app's dark-mode class with the system theme. */
    private fun injectDarkModeIfNeeded() {
        val isDark = (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val js = if (isDark) {
            """
            (function(){
              if(!document.body.classList.contains('dark')){
                document.body.classList.add('dark');
                var btn=document.getElementById('dark-btn');
                if(btn) btn.textContent='☀️ light';
                localStorage.setItem('theme','dark');
              }
            })();
            """.trimIndent()
        } else {
            """
            (function(){
              if(localStorage.getItem('theme')==='dark') return; // respect user choice
            })();
            """.trimIndent()
        }
        binding.webView.evaluateJavascript(js, null)
    }

    // ── Back navigation ───────────────────────────────────────────────────────
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    override fun onDestroy() {
        binding.webView.apply {
            stopLoading()
            clearHistory()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }
}
