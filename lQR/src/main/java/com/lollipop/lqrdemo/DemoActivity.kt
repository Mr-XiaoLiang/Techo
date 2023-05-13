package com.lollipop.lqrdemo

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.databinding.ActivityDemoBinding
import com.lollipop.lqrdemo.databinding.ItemDemoBinding
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment
import kotlin.random.Random
import kotlin.random.nextInt

class DemoActivity : AppCompatActivity() {

    private val binding: ActivityDemoBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val data = ArrayList<Pigment>()

        val colorRange = 0..255

        for (i in 0..50) {
            val color1 = Color.valueOf(
                Color.rgb(
                    Random.nextInt(colorRange),
                    Random.nextInt(colorRange),
                    Random.nextInt(colorRange)
                )
            )
            val color2 = Color.valueOf(
                Color.rgb(
                    Random.nextInt(colorRange),
                    Random.nextInt(colorRange),
                    Random.nextInt(colorRange)
                )
            )
            data.add(Pigment(color1, color2, BlendMode.Light))
            data.add(Pigment(color1, color2, BlendMode.Dark))
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = DemoAdapter(data)

    }

    private class DemoAdapter(private val data: List<Pigment>) :
        RecyclerView.Adapter<DemoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoHolder {
            return DemoHolder.create(parent)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: DemoHolder, position: Int) {
            holder.bind(data[position])
        }

    }

    private class DemoHolder(
        private val binding: ItemDemoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun create(parent: ViewGroup): DemoHolder {
                return DemoHolder(parent.bind(false))
            }
        }

        fun bind(pigment: Pigment) {
            binding.card1.setBackgroundColor(pigment.primaryColor)
            binding.card2.setBackgroundColor(pigment.secondaryColor)
            binding.root.setBackgroundColor(pigment.background)

            binding.title1.setTextColor(pigment.onPrimaryTitle)
            binding.title2.setTextColor(pigment.onSecondaryTitle)
            binding.title3.setTextColor(pigment.onBackgroundTitle)

            binding.content1.setTextColor(pigment.onPrimaryBody)
            binding.content2.setTextColor(pigment.onSecondaryBody)
            binding.content3.setTextColor(pigment.onBackgroundBody)
        }

    }

}